package com.pulsepass.notificationservice.domain.exceptions;

public class NotificationEventTypeNotFoundException extends DomainException {

    public NotificationEventTypeNotFoundException() {
        super("Notification event type not found");
    }

    @Override
    public String getCode() {
        return "NTF00004";
    }

    @Override
    public String getMessageKey() {
        return "error.notification.notification_event_type_not_found";
    }
}
