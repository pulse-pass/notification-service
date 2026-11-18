package com.pulsepass.notificationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.entities.EmailMessageHistory;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageHistoryEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageStatusEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventTypeEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationOriginEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.EmailMessageHistoryRepositoryImpl;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.JpaEmailMessageHistoryRepository;
import org.junit.jupiter.api.Test;

class EmailMessageHistoryRepositoryImplTest {

    private final JpaEmailMessageHistoryRepository jpaRepository = mock(JpaEmailMessageHistoryRepository.class);
    private final EmailMessageHistoryRepositoryImpl repository = new EmailMessageHistoryRepositoryImpl(jpaRepository);

    @Test
    void save_newHistory_returnsSavedDomain() {
        var status = new EmailMessageStatusEntity(1L, "PENDING", null);
        var event = new NotificationEventEntity(
                1L, "event-key", null, "origin-key",
                new NotificationEventTypeEntity(1L, "ORGANIZER_APPROVED", "s", "t", "legal_name", null),
                new NotificationOriginEntity(1L, "ORGANIZATION", null),
                "{}", null, null);
        var message = new EmailMessageEntity(2L, "message-key", event, null, "{}", status, null, null);
        when(jpaRepository.save(any(EmailMessageHistoryEntity.class)))
                .thenReturn(new EmailMessageHistoryEntity(4L, "history-key", message, null, status, "{}", null));
        var domainEvent = new NotificationEvent();
        domainEvent.setNotificationEventType(new NotificationEventType());
        domainEvent.setNotificationOrigin(new NotificationOrigin());
        var domainMessage = new EmailMessage("message-key", domainEvent, "{}", new EmailMessageStatus());

        var result = repository.save(EmailMessageHistory.fromEmailMessage(domainMessage, null));

        assertThat(result.getId()).isEqualTo(4L);
    }
}
