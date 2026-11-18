package com.pulsepass.notificationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.exceptions.NotificationEventLockedException;
import org.junit.jupiter.api.Test;

class NotificationEventLockedExceptionTest {

    @Test
    void getCode_always_returnsServiceCode() {
        assertThat(new NotificationEventLockedException().getCode()).isEqualTo("NTF00002");
    }

    @Test
    void getMessageKey_always_returnsCapabilityKey() {
        assertThat(new NotificationEventLockedException().getMessageKey())
                .isEqualTo("error.notification.notification_event_locked");
    }
}
