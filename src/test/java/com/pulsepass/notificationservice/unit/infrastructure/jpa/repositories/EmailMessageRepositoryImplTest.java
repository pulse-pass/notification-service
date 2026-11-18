package com.pulsepass.notificationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageStatusEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventTypeEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationOriginEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.EmailMessageRepositoryImpl;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.JpaEmailMessageRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EmailMessageRepositoryImplTest {

    private final JpaEmailMessageRepository jpaRepository = mock(JpaEmailMessageRepository.class);
    private final EmailMessageRepositoryImpl repository = new EmailMessageRepositoryImpl(jpaRepository);

    private EmailMessageEntity entity() {
        var event = new NotificationEventEntity(
                1L, "event-key", null, "origin-key",
                new NotificationEventTypeEntity(1L, "ORGANIZER_APPROVED", "s", "t", "legal_name", null),
                new NotificationOriginEntity(1L, "ORGANIZATION", null),
                "{}", null, null);
        return new EmailMessageEntity(
                2L, "message-key", event, null, "{}", new EmailMessageStatusEntity(1L, "PENDING", null), null, null);
    }

    @Test
    void save_newMessage_returnsSavedDomain() {
        when(jpaRepository.save(any(EmailMessageEntity.class))).thenReturn(entity());
        var event = new NotificationEvent();
        event.setNotificationEventType(new NotificationEventType());
        event.setNotificationOrigin(new NotificationOrigin());
        var message = new EmailMessage("message-key", event, "{}", new EmailMessageStatus());

        var result = repository.save(message);

        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void findByNotificationEventId_existingEvent_returnsDomain() {
        when(jpaRepository.findByNotificationEventId(1L)).thenReturn(Optional.of(entity()));

        assertThat(repository.findByNotificationEventId(1L)).isPresent();
    }

    @Test
    void findByNotificationEventId_unknownEvent_returnsEmpty() {
        when(jpaRepository.findByNotificationEventId(9L)).thenReturn(Optional.empty());

        assertThat(repository.findByNotificationEventId(9L)).isEmpty();
    }
}
