package com.pulsepass.notificationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.entities.EmailMessageAttempt;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EmailMessageAttemptTest {

    private final EmailMessage emailMessage = new EmailMessage();

    @Test
    void constructor_withoutId_setsGivenFieldsAndLeavesIdAndDatesNull() {
        var attempt = new EmailMessageAttempt("attempt-key", emailMessage, "refused");

        assertThat(attempt.getId()).isNull();
        assertThat(attempt.getEmailMessageAttemptKey()).isEqualTo("attempt-key");
        assertThat(attempt.getEmailMessage()).isSameAs(emailMessage);
        assertThat(attempt.getErrorCode()).isEqualTo("refused");
        assertThat(attempt.getCreatedAt()).isNull();
        assertThat(attempt.getUpdatedAt()).isNull();
    }

    @Test
    void constructor_withNullResponse_keepsNull() {
        var attempt = new EmailMessageAttempt("attempt-key", emailMessage, null);

        assertThat(attempt.getErrorCode()).isNull();
    }

    @Test
    void constructor_withAllFields_setsEveryField() {
        var createdAt = LocalDateTime.of(2026, 11, 17, 10, 0);
        var updatedAt = createdAt.plusMinutes(1);

        var attempt = new EmailMessageAttempt(5L, "attempt-key", emailMessage, "queued", createdAt, updatedAt);

        assertThat(attempt.getId()).isEqualTo(5L);
        assertThat(attempt.getErrorCode()).isEqualTo("queued");
        assertThat(attempt.getCreatedAt()).isEqualTo(createdAt);
        assertThat(attempt.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void setters_givenValues_updateFields() {
        var attempt = new EmailMessageAttempt();
        var now = LocalDateTime.of(2026, 11, 17, 10, 0);

        attempt.setId(9L);
        attempt.setEmailMessageAttemptKey("attempt-key");
        attempt.setEmailMessage(emailMessage);
        attempt.setErrorCode("error");
        attempt.setCreatedAt(now);
        attempt.setUpdatedAt(now);

        assertThat(attempt.getId()).isEqualTo(9L);
        assertThat(attempt.getEmailMessageAttemptKey()).isEqualTo("attempt-key");
        assertThat(attempt.getEmailMessage()).isSameAs(emailMessage);
        assertThat(attempt.getErrorCode()).isEqualTo("error");
        assertThat(attempt.getCreatedAt()).isEqualTo(now);
        assertThat(attempt.getUpdatedAt()).isEqualTo(now);
    }
}
