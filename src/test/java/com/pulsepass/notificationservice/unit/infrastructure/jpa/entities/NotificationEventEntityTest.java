package com.pulsepass.notificationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationEventEntityTest {

    @Test
    void toDomain_afterFromDomain_keepsAllFields() {
        var at = LocalDateTime.of(2026, 11, 17, 10, 0);
        var domain = new NotificationEvent(
                7L, "event-key", "person-key", "origin-key",
                new NotificationEventType(1L, "ORGANIZER_APPROVED", "subject", "template", "legal_name", at),
                new NotificationOrigin(2L, "ORGANIZATION", at),
                "{\"origin\":\"ORGANIZATION\"}", at, at);

        var result = NotificationEventEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void toDomain_withoutPersonKey_keepsNull() {
        var domain = new NotificationEvent();
        domain.setNotificationEventType(new NotificationEventType());
        domain.setNotificationOrigin(new NotificationOrigin());

        var result = NotificationEventEntity.fromDomain(domain).toDomain();

        assertThat(result.getPersonKey()).isNull();
    }
}
