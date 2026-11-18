package com.pulsepass.notificationservice.domain.exceptions;

public class EmailSendingException extends DomainException {

    public EmailSendingException() {
        super("Email sending failed");
    }

    @Override
    public String getCode() {
        return "NTF00007";
    }

    @Override
    public String getMessageKey() {
        return "error.notification.email_sending";
    }
}
