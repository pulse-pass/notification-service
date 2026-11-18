package com.pulsepass.notificationservice.domain.entities;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmailMessage {

    private Long id;
    private String emailMessageKey;
    private NotificationEvent notificationEvent;
    private String externalMessageKey;
    private String emailData;
    private EmailMessageStatus emailMessageStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmailMessage() {
    }

    public EmailMessage(
            String emailMessageKey,
            NotificationEvent notificationEvent,
            String emailData,
            EmailMessageStatus emailMessageStatus
    ) {
        this.emailMessageKey = emailMessageKey;
        this.notificationEvent = notificationEvent;
        this.emailData = emailData;
        this.emailMessageStatus = emailMessageStatus;
    }

    public EmailMessage(
            Long id,
            String emailMessageKey,
            NotificationEvent notificationEvent,
            String externalMessageKey,
            String emailData,
            EmailMessageStatus emailMessageStatus,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.emailMessageKey = emailMessageKey;
        this.notificationEvent = notificationEvent;
        this.externalMessageKey = externalMessageKey;
        this.emailData = emailData;
        this.emailMessageStatus = emailMessageStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailMessageKey() {
        return this.emailMessageKey;
    }

    public void setEmailMessageKey(String emailMessageKey) {
        this.emailMessageKey = emailMessageKey;
    }

    public NotificationEvent getNotificationEvent() {
        return this.notificationEvent;
    }

    public void setNotificationEvent(NotificationEvent notificationEvent) {
        this.notificationEvent = notificationEvent;
    }

    public String getExternalMessageKey() {
        return this.externalMessageKey;
    }

    public void setExternalMessageKey(String externalMessageKey) {
        this.externalMessageKey = externalMessageKey;
    }

    public String getEmailData() {
        return this.emailData;
    }

    public void setEmailData(String emailData) {
        this.emailData = emailData;
    }

    public EmailMessageStatus getEmailMessageStatus() {
        return this.emailMessageStatus;
    }

    public void setEmailMessageStatus(EmailMessageStatus emailMessageStatus) {
        this.emailMessageStatus = emailMessageStatus;
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

    public Map<String, Object> toHistoryData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("email_message_key", this.emailMessageKey);
        data.put("notification_event_key", this.notificationEvent.getNotificationEventKey());
        data.put("email_message_status", this.emailMessageStatus.getEnumerator());
        return data;
    }
}
