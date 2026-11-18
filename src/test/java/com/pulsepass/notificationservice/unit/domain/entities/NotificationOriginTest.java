package com.pulsepass.notificationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationOriginTest {

    @Test
    void constructor_withAllFields_setsEveryField() {
        var createdAt = LocalDateTime.of(2026, 11, 17, 10, 0);

        var entity = new NotificationOrigin(1L, "VALUE", createdAt);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getEnumerator()).isEqualTo("VALUE");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void setters_givenValues_updateFields() {
        var entity = new NotificationOrigin();
        var createdAt = LocalDateTime.of(2026, 11, 17, 10, 0);

        entity.setId(2L);
        entity.setEnumerator("OTHER");
        entity.setCreatedAt(createdAt);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getEnumerator()).isEqualTo("OTHER");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }
}
