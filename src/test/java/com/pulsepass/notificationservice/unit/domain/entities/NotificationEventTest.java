package com.pulsepass.notificationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationEventTest {

    private final NotificationEventType type = new NotificationEventType();
    private final NotificationOrigin origin = new NotificationOrigin(1L, "ORGANIZATION", null);

    @Test
    void constructor_withoutId_setsGivenFieldsAndLeavesIdAndDatesNull() {
        var event = new NotificationEvent("event-key", "person-key", "origin-key", type, origin, "{}");

        assertThat(event.getId()).isNull();
        assertThat(event.getNotificationEventKey()).isEqualTo("event-key");
        assertThat(event.getPersonKey()).isEqualTo("person-key");
        assertThat(event.getOriginKey()).isEqualTo("origin-key");
        assertThat(event.getNotificationEventType()).isSameAs(type);
        assertThat(event.getNotificationOrigin()).isSameAs(origin);
        assertThat(event.getData()).isEqualTo("{}");
        assertThat(event.getCreatedAt()).isNull();
        assertThat(event.getUpdatedAt()).isNull();
    }

    @Test
    void constructor_withAllFields_setsEveryField() {
        var createdAt = LocalDateTime.of(2026, 11, 17, 10, 0);
        var updatedAt = createdAt.plusMinutes(1);

        var event = new NotificationEvent(
                7L, "event-key", null, "origin-key", type, origin, "{}", createdAt, updatedAt);

        assertThat(event.getId()).isEqualTo(7L);
        assertThat(event.getPersonKey()).isNull();
        assertThat(event.getCreatedAt()).isEqualTo(createdAt);
        assertThat(event.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void setters_givenValues_updateFields() {
        var event = new NotificationEvent();
        var now = LocalDateTime.of(2026, 11, 17, 10, 0);

        event.setId(3L);
        event.setNotificationEventKey("event-key");
        event.setPersonKey("person-key");
        event.setOriginKey("origin-key");
        event.setNotificationEventType(type);
        event.setNotificationOrigin(origin);
        event.setData("{\"a\":1}");
        event.setCreatedAt(now);
        event.setUpdatedAt(now);

        assertThat(event.getId()).isEqualTo(3L);
        assertThat(event.getNotificationEventKey()).isEqualTo("event-key");
        assertThat(event.getPersonKey()).isEqualTo("person-key");
        assertThat(event.getOriginKey()).isEqualTo("origin-key");
        assertThat(event.getNotificationEventType()).isSameAs(type);
        assertThat(event.getNotificationOrigin()).isSameAs(origin);
        assertThat(event.getData()).isEqualTo("{\"a\":1}");
        assertThat(event.getCreatedAt()).isEqualTo(now);
        assertThat(event.getUpdatedAt()).isEqualTo(now);
    }
}
