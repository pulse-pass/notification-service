package com.pulsepass.notificationservice.domain.entities;

import java.time.LocalDateTime;

public class NotificationEvent {

    private Long id;
    private String notificationEventKey;
    private String personKey;
    private String originKey;
    private NotificationEventType notificationEventType;
    private NotificationOrigin notificationOrigin;
    private String data;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NotificationEvent() {
    }

    public NotificationEvent(
            String notificationEventKey,
            String personKey,
            String originKey,
            NotificationEventType notificationEventType,
            NotificationOrigin notificationOrigin,
            String data
    ) {
        this.notificationEventKey = notificationEventKey;
        this.personKey = personKey;
        this.originKey = originKey;
        this.notificationEventType = notificationEventType;
        this.notificationOrigin = notificationOrigin;
        this.data = data;
    }

    public NotificationEvent(
            Long id,
            String notificationEventKey,
            String personKey,
            String originKey,
            NotificationEventType notificationEventType,
            NotificationOrigin notificationOrigin,
            String data,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.notificationEventKey = notificationEventKey;
        this.personKey = personKey;
        this.originKey = originKey;
        this.notificationEventType = notificationEventType;
        this.notificationOrigin = notificationOrigin;
        this.data = data;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNotificationEventKey() {
        return this.notificationEventKey;
    }

    public void setNotificationEventKey(String notificationEventKey) {
        this.notificationEventKey = notificationEventKey;
    }

    public String getPersonKey() {
        return this.personKey;
    }

    public void setPersonKey(String personKey) {
        this.personKey = personKey;
    }

    public String getOriginKey() {
        return this.originKey;
    }

    public void setOriginKey(String originKey) {
        this.originKey = originKey;
    }

    public NotificationEventType getNotificationEventType() {
        return this.notificationEventType;
    }

    public void setNotificationEventType(NotificationEventType notificationEventType) {
        this.notificationEventType = notificationEventType;
    }

    public NotificationOrigin getNotificationOrigin() {
        return this.notificationOrigin;
    }

    public void setNotificationOrigin(NotificationOrigin notificationOrigin) {
        this.notificationOrigin = notificationOrigin;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
