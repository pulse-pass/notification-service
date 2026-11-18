package com.pulsepass.notificationservice.domain.entities;

import java.time.LocalDateTime;

public class EmailMessageAttempt {

    private Long id;
    private String emailMessageAttemptKey;
    private EmailMessage emailMessage;
    private String errorCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmailMessageAttempt() {
    }

    public EmailMessageAttempt(
            String emailMessageAttemptKey,
            EmailMessage emailMessage,
            String errorCode
    ) {
        this.emailMessageAttemptKey = emailMessageAttemptKey;
        this.emailMessage = emailMessage;
        this.errorCode = errorCode;
    }

    public EmailMessageAttempt(
            Long id,
            String emailMessageAttemptKey,
            EmailMessage emailMessage,
            String errorCode,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.emailMessageAttemptKey = emailMessageAttemptKey;
        this.emailMessage = emailMessage;
        this.errorCode = errorCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailMessageAttemptKey() {
        return this.emailMessageAttemptKey;
    }

    public void setEmailMessageAttemptKey(String emailMessageAttemptKey) {
        this.emailMessageAttemptKey = emailMessageAttemptKey;
    }

    public EmailMessage getEmailMessage() {
        return this.emailMessage;
    }

    public void setEmailMessage(EmailMessage emailMessage) {
        this.emailMessage = emailMessage;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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
