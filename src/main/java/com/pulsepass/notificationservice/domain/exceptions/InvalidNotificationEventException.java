package com.pulsepass.notificationservice.domain.exceptions;

public class InvalidNotificationEventException extends DomainException {

    public InvalidNotificationEventException() {
        super("Invalid notification event");
    }

    @Override
    public String getCode() {
        return "NTF00003";
    }

    @Override
    public String getMessageKey() {
        return "error.notification.invalid_notification_event";
    }
}
