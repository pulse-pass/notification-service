package com.pulsepass.notificationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventTypeEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationEventTypeEntityTest {

    @Test
    void toDomain_afterFromDomain_keepsAllFields() {
        var at = LocalDateTime.of(2026, 11, 17, 10, 0);
        var domain = new NotificationEventType(
                1L, "ORGANIZER_APPROVED", "subject", "organizer-approved", "legal_name", at);

        var result = NotificationEventTypeEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
