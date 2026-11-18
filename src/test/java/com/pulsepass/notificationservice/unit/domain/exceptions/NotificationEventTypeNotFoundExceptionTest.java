package com.pulsepass.notificationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.exceptions.NotificationEventTypeNotFoundException;
import org.junit.jupiter.api.Test;

class NotificationEventTypeNotFoundExceptionTest {

    @Test
    void getCode_always_returnsServiceCode() {
        assertThat(new NotificationEventTypeNotFoundException().getCode()).isEqualTo("NTF00004");
    }

    @Test
    void getMessageKey_always_returnsCapabilityKey() {
        assertThat(new NotificationEventTypeNotFoundException().getMessageKey())
                .isEqualTo("error.notification.notification_event_type_not_found");
    }
}
