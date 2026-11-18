package com.pulsepass.notificationservice.domain.exceptions;

public class EmailRefusedException extends DomainException {

    public EmailRefusedException() {
        super("Email was refused by the provider");
    }

    @Override
    public String getCode() {
        return "NTF00001";
    }

    @Override
    public String getMessageKey() {
        return "error.notification.email_refused";
    }
}
