package com.pulsepass.notificationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationOriginEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationOriginEntityTest {

    @Test
    void toDomain_afterFromDomain_keepsAllFields() {
        var domain = new NotificationOrigin(1L, "ORGANIZATION", LocalDateTime.of(2026, 11, 17, 10, 0));

        var result = NotificationOriginEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
