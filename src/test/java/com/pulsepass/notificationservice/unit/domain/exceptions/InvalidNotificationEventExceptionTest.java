package com.pulsepass.notificationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.exceptions.InvalidNotificationEventException;
import org.junit.jupiter.api.Test;

class InvalidNotificationEventExceptionTest {

    @Test
    void getCode_always_returnsServiceCode() {
        assertThat(new InvalidNotificationEventException().getCode()).isEqualTo("NTF00003");
    }

    @Test
    void getMessageKey_always_returnsCapabilityKey() {
        assertThat(new InvalidNotificationEventException().getMessageKey())
                .isEqualTo("error.notification.invalid_notification_event");
    }
}
