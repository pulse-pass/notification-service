package com.pulsepass.notificationservice.domain.exceptions;

public class NotificationEventLockedException extends DomainException {

    public NotificationEventLockedException() {
        super("Notification event is being processed");
    }

    @Override
    public String getCode() {
        return "NTF00002";
    }

    @Override
    public String getMessageKey() {
        return "error.notification.notification_event_locked";
    }
}
