package com.pulsepass.notificationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.exceptions.EmailSendingException;
import org.junit.jupiter.api.Test;

class EmailSendingExceptionTest {

    @Test
    void getCode_always_returnsServiceCode() {
        assertThat(new EmailSendingException().getCode()).isEqualTo("NTF00007");
    }

    @Test
    void getMessageKey_always_returnsCapabilityKey() {
        assertThat(new EmailSendingException().getMessageKey())
                .isEqualTo("error.notification.email_sending");
    }

    @Test
    void getMessage_always_returnsLiteralMessage() {
        assertThat(new EmailSendingException().getMessage()).isEqualTo("Email sending failed");
    }
}
