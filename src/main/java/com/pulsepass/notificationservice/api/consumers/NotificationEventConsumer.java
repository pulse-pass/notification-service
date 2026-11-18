package com.pulsepass.notificationservice.api.consumers;

import static com.pulsepass.notificationservice.api.messages.NotificationEventMessageTypeEnum.NOTIFICATION_EVENT_CREATION;

import com.pulsepass.notificationservice.api.messages.NotificationEventMessage;
import com.pulsepass.notificationservice.application.services.NotificationEventService;
import com.pulsepass.notificationservice.domain.Cache;
import com.pulsepass.notificationservice.domain.exceptions.NotificationEventLockedException;
import com.pulsepass.notificationservice.domain.exceptions.UnsupportedNotificationEventMessageTypeException;
import jakarta.validation.Valid;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private static final String LOCK_KEY_PREFIX = "notification-event:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(60);

    private final NotificationEventService notificationEventService;
    private final Cache cache;

    public NotificationEventConsumer(NotificationEventService notificationEventService, Cache cache) {
        this.notificationEventService = notificationEventService;
        this.cache = cache;
    }

    @KafkaListener(
            id = "notification-service-notification_event-consumer",
            topics = "notification_event"
    )
    public void consume(@Payload @Valid NotificationEventMessage message) {
        logger.info("NotificationEventConsumer.consume - Starting");

        String lockKey = LOCK_KEY_PREFIX + message.eventData().notificationEventKey();
        if (!cache.acquireLock(lockKey, LOCK_TTL)) {
            logger.warn("NotificationEventConsumer.consume - Notification event is locked");
            throw new NotificationEventLockedException();
        }

        try {
            if (message.eventType() == NOTIFICATION_EVENT_CREATION) {
                notificationEventService.processNotificationEventCreation(message.toDTO());
            } else {
                logger.warn("NotificationEventConsumer.consume - Message type is not supported");
                throw new UnsupportedNotificationEventMessageTypeException();
            }
        } catch (Exception ex) {
            cache.delete(lockKey);
            throw ex;
        }
        cache.delete(lockKey);
        logger.info("NotificationEventConsumer.consume - Successfully finished");
    }
}
