package com.pulsepass.notificationservice.domain.entities;

import com.pulsepass.notificationservice.common.JsonHandlers;
import java.time.LocalDateTime;
import java.util.UUID;

public class EmailMessageHistory {

    private Long id;
    private String emailMessageHistoryKey;
    private EmailMessage emailMessage;
    private EmailMessageStatus oldStatus;
    private EmailMessageStatus newStatus;
    private String historyData;
    private LocalDateTime createdAt;

    public EmailMessageHistory() {
    }

    public EmailMessageHistory(
            String emailMessageHistoryKey,
            EmailMessage emailMessage,
            EmailMessageStatus oldStatus,
            EmailMessageStatus newStatus,
            String historyData
    ) {
        this.emailMessageHistoryKey = emailMessageHistoryKey;
        this.emailMessage = emailMessage;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.historyData = historyData;
    }

    public EmailMessageHistory(
            Long id,
            String emailMessageHistoryKey,
            EmailMessage emailMessage,
            EmailMessageStatus oldStatus,
            EmailMessageStatus newStatus,
            String historyData,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.emailMessageHistoryKey = emailMessageHistoryKey;
        this.emailMessage = emailMessage;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.historyData = historyData;
        this.createdAt = createdAt;
    }

    public static EmailMessageHistory fromEmailMessage(EmailMessage emailMessage, EmailMessageStatus oldStatus) {
        return new EmailMessageHistory(
                UUID.randomUUID().toString(),
                emailMessage,
                oldStatus,
                emailMessage.getEmailMessageStatus(),
                JsonHandlers.toJson(emailMessage.toHistoryData())
        );
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailMessageHistoryKey() {
        return this.emailMessageHistoryKey;
    }

    public void setEmailMessageHistoryKey(String emailMessageHistoryKey) {
        this.emailMessageHistoryKey = emailMessageHistoryKey;
    }

    public EmailMessage getEmailMessage() {
        return this.emailMessage;
    }

    public void setEmailMessage(EmailMessage emailMessage) {
        this.emailMessage = emailMessage;
    }

    public EmailMessageStatus getOldStatus() {
        return this.oldStatus;
    }

    public void setOldStatus(EmailMessageStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public EmailMessageStatus getNewStatus() {
        return this.newStatus;
    }

    public void setNewStatus(EmailMessageStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getHistoryData() {
        return this.historyData;
    }

    public void setHistoryData(String historyData) {
        this.historyData = historyData;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
