package com.pulsepass.notificationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.entities.EmailMessageHistory;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageHistoryEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EmailMessageHistoryEntityTest {

    private EmailMessage message() {
        var at = LocalDateTime.of(2026, 11, 17, 10, 0);
        var event = new NotificationEvent(
                7L, "event-key", "person-key", "origin-key",
                new NotificationEventType(1L, "ORGANIZER_APPROVED", "subject", "template", "legal_name", at),
                new NotificationOrigin(2L, "ORGANIZATION", at),
                "{}", at, at);
        return new EmailMessage(9L, "message-key", event, null, "{}", new EmailMessageStatus(3L, "SENT", at), at, at);
    }

    @Test
    void toDomain_afterFromDomain_keepsAllFields() {
        var at = LocalDateTime.of(2026, 11, 17, 10, 0);
        var domain = new EmailMessageHistory(
                5L, "history-key", message(), new EmailMessageStatus(1L, "PENDING", at),
                new EmailMessageStatus(3L, "SENT", at), "{}", at);

        var result = EmailMessageHistoryEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void toDomain_creationWithoutOldStatus_keepsOldStatusNull() {
        var domain = new EmailMessageHistory(
                "history-key", message(), null, new EmailMessageStatus(3L, "PENDING", null), "{}");

        var result = EmailMessageHistoryEntity.fromDomain(domain).toDomain();

        assertThat(result.getOldStatus()).isNull();
    }
}
