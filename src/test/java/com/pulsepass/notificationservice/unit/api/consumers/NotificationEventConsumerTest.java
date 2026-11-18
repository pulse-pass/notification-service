package com.pulsepass.notificationservice.unit.api.consumers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.api.consumers.NotificationEventConsumer;
import com.pulsepass.notificationservice.api.messages.NotificationEventMessage;
import com.pulsepass.notificationservice.api.messages.NotificationEventMessageTypeEnum;
import com.pulsepass.notificationservice.api.messages.definitions.EmailContent;
import com.pulsepass.notificationservice.api.messages.definitions.EventData;
import com.pulsepass.notificationservice.api.messages.definitions.ReceiverContact;
import com.pulsepass.notificationservice.application.dtos.input.ProcessNotificationEventDTO;
import com.pulsepass.notificationservice.application.services.NotificationEventService;
import com.pulsepass.notificationservice.domain.Cache;
import com.pulsepass.notificationservice.domain.exceptions.InvalidNotificationEventException;
import com.pulsepass.notificationservice.domain.exceptions.NotificationEventLockedException;
import com.pulsepass.notificationservice.domain.exceptions.UnsupportedNotificationEventMessageTypeException;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.annotation.KafkaListener;

class NotificationEventConsumerTest {

    private static final String LOCK_KEY = "notification-event:event-key";
    private static final Duration LOCK_TTL = Duration.ofSeconds(60);

    private Cache cache;
    private NotificationEventService notificationEventService;
    private NotificationEventConsumer consumer;
    private NotificationEventMessage message;

    @BeforeEach
    void setUp() {
        notificationEventService = mock(NotificationEventService.class);
        cache = mock(Cache.class);
        when(cache.acquireLock(LOCK_KEY, LOCK_TTL)).thenReturn(true);
        consumer = new NotificationEventConsumer(notificationEventService, cache);
        message = new NotificationEventMessage(
                NotificationEventMessageTypeEnum.NOTIFICATION_EVENT_CREATION,
                "event-key",
                new EventData(
                        "event-key", "ORGANIZER_APPROVED", "person-key", "ORGANIZATION", "origin-key",
                        new ReceiverContact("Maria", "maria@empresa.com"),
                        new EmailContent(Map.of("legal_name", "Eventos"))));
    }

    @Test
    void consume_lockAcquired_callsServiceWithDtoAndReleasesLock() {
        consumer.consume(message);

        var order = inOrder(cache, notificationEventService);
        order.verify(cache).acquireLock(LOCK_KEY, LOCK_TTL);
        order.verify(notificationEventService).processNotificationEventCreation(message.toDTO());
        order.verify(cache).delete(LOCK_KEY);
    }

    @Test
    void consume_lockNotAcquired_throwsLockedAndDoesNotCallService() {
        when(cache.acquireLock(LOCK_KEY, LOCK_TTL)).thenReturn(false);

        assertThatThrownBy(() -> consumer.consume(message)).isInstanceOf(NotificationEventLockedException.class);

        verifyNoInteractions(notificationEventService);
        verify(cache, never()).delete(anyString());
    }

    @Test
    void consume_serviceThrows_releasesLockAndRethrows() {
        var failure = new InvalidNotificationEventException();
        doThrow(failure).when(notificationEventService)
                .processNotificationEventCreation(any(ProcessNotificationEventDTO.class));

        assertThatThrownBy(() -> consumer.consume(message)).isSameAs(failure);

        verify(cache).delete(LOCK_KEY);
    }

    @Test
    void consume_nullEnvelopeEventType_throwsUnsupportedReleasesLockAndDoesNotCallService() {
        var other = new NotificationEventMessage(null, "event-key", message.eventData());

        assertThatThrownBy(() -> consumer.consume(other))
                .isInstanceOf(UnsupportedNotificationEventMessageTypeException.class);

        verifyNoInteractions(notificationEventService);
        verify(cache).delete(LOCK_KEY);
    }

    @Test
    void consume_listenerAnnotation_declaresTopicAndConsumerId() throws Exception {
        var method = NotificationEventConsumer.class.getMethod("consume", NotificationEventMessage.class);

        var listener = method.getAnnotation(KafkaListener.class);

        assertThat(listener.id()).isEqualTo("notification-service-notification_event-consumer");
        assertThat(listener.topics()).containsExactly("notification_event");
    }
}
