package com.pulsepass.notificationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.entities.EmailMessageAttempt;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageAttemptEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EmailMessageAttemptEntityTest {

    @Test
    void toDomain_afterFromDomain_keepsAllFields() {
        var at = LocalDateTime.of(2026, 11, 17, 10, 0);
        var event = new NotificationEvent(
                7L, "event-key", "person-key", "origin-key",
                new NotificationEventType(1L, "ORGANIZER_APPROVED", "subject", "template", "legal_name", at),
                new NotificationOrigin(2L, "ORGANIZATION", at),
                "{}", at, at);
        var message = new EmailMessage(
                9L, "message-key", event, null, "{}", new EmailMessageStatus(3L, "PENDING", at), at, at);
        var domain = new EmailMessageAttempt(11L, "attempt-key", message, "451 refused", at, at);

        var result = EmailMessageAttemptEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
