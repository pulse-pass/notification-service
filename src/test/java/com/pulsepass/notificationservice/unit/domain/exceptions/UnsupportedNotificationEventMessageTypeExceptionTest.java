package com.pulsepass.notificationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.exceptions.UnsupportedNotificationEventMessageTypeException;
import org.junit.jupiter.api.Test;

class UnsupportedNotificationEventMessageTypeExceptionTest {

    @Test
    void getCode_always_returnsServiceCode() {
        assertThat(new UnsupportedNotificationEventMessageTypeException().getCode()).isEqualTo("NTF00005");
    }

    @Test
    void getMessageKey_always_returnsCapabilityKey() {
        assertThat(new UnsupportedNotificationEventMessageTypeException().getMessageKey())
                .isEqualTo("error.notification.unsupported_notification_event_message_type");
    }
}
