package com.pulsepass.notificationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.exceptions.EmailProviderUnavailableException;
import org.junit.jupiter.api.Test;

class EmailProviderUnavailableExceptionTest {

    @Test
    void getCode_always_returnsServiceCode() {
        assertThat(new EmailProviderUnavailableException().getCode()).isEqualTo("NTF00006");
    }

    @Test
    void getMessageKey_always_returnsCapabilityKey() {
        assertThat(new EmailProviderUnavailableException().getMessageKey())
                .isEqualTo("error.notification.email_provider_unavailable");
    }

    @Test
    void getMessage_always_returnsLiteralMessage() {
        assertThat(new EmailProviderUnavailableException().getMessage()).isEqualTo("Email provider is unavailable");
    }
}
