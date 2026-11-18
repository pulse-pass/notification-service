package com.pulsepass.notificationservice.domain.exceptions;

public class EmailProviderUnavailableException extends DomainException {

    public EmailProviderUnavailableException() {
        super("Email provider is unavailable");
    }

    @Override
    public String getCode() {
        return "NTF00006";
    }

    @Override
    public String getMessageKey() {
        return "error.notification.email_provider_unavailable";
    }
}
