package com.pulsepass.notificationservice.domain.exceptions;

public class UnsupportedNotificationEventMessageTypeException extends DomainException {

    public UnsupportedNotificationEventMessageTypeException() {
        super("Notification event message type is not supported");
    }

    @Override
    public String getCode() {
        return "NTF00005";
    }

    @Override
    public String getMessageKey() {
        return "error.notification.unsupported_notification_event_message_type";
    }
}
