package com.pulsepass.notificationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageStatusEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EmailMessageStatusEntityTest {

    @Test
    void toDomain_afterFromDomain_keepsAllFields() {
        var domain = new EmailMessageStatus(1L, "PENDING", LocalDateTime.of(2026, 11, 17, 10, 0));

        var result = EmailMessageStatusEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
