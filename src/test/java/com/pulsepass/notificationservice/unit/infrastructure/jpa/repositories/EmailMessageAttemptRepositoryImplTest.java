package com.pulsepass.notificationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.entities.EmailMessageAttempt;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageAttemptEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageStatusEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventTypeEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationOriginEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.EmailMessageAttemptRepositoryImpl;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.JpaEmailMessageAttemptRepository;
import org.junit.jupiter.api.Test;

class EmailMessageAttemptRepositoryImplTest {

    private final JpaEmailMessageAttemptRepository jpaRepository = mock(JpaEmailMessageAttemptRepository.class);
    private final EmailMessageAttemptRepositoryImpl repository = new EmailMessageAttemptRepositoryImpl(jpaRepository);

    @Test
    void save_newAttempt_returnsSavedDomain() {
        var event = new NotificationEventEntity(
                1L, "event-key", null, "origin-key",
                new NotificationEventTypeEntity(1L, "ORGANIZER_APPROVED", "s", "t", "legal_name", null),
                new NotificationOriginEntity(1L, "ORGANIZATION", null),
                "{}", null, null);
        var message = new EmailMessageEntity(
                2L, "message-key", event, null, "{}", new EmailMessageStatusEntity(1L, "PENDING", null), null, null);
        when(jpaRepository.save(any(EmailMessageAttemptEntity.class)))
                .thenReturn(new EmailMessageAttemptEntity(3L, "attempt-key", message, "NTF00006", null, null));
        var domainEvent = new NotificationEvent();
        domainEvent.setNotificationEventType(new NotificationEventType());
        domainEvent.setNotificationOrigin(new NotificationOrigin());
        var domainMessage = new EmailMessage("message-key", domainEvent, "{}", new EmailMessageStatus());

        var result = repository.save(new EmailMessageAttempt("attempt-key", domainMessage, "NTF00006"));

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getErrorCode()).isEqualTo("NTF00006");
    }

    @Test
    void countByEmailMessageId_existingMessage_returnsJpaCount() {
        when(jpaRepository.countByEmailMessageId(2L)).thenReturn(4L);

        assertThat(repository.countByEmailMessageId(2L)).isEqualTo(4L);
    }
}
