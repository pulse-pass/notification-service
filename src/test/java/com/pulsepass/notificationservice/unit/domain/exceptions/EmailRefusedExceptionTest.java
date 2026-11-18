package com.pulsepass.notificationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.exceptions.EmailRefusedException;
import org.junit.jupiter.api.Test;

class EmailRefusedExceptionTest {

    @Test
    void getCode_always_returnsServiceCode() {
        assertThat(new EmailRefusedException().getCode()).isEqualTo("NTF00001");
    }

    @Test
    void getMessageKey_always_returnsCapabilityKey() {
        assertThat(new EmailRefusedException().getMessageKey()).isEqualTo("error.notification.email_refused");
    }

    @Test
    void getMessage_always_returnsLiteralMessage() {
        assertThat(new EmailRefusedException().getMessage()).isEqualTo("Email was refused by the provider");
    }
}
