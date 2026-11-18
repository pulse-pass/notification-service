package com.pulsepass.notificationservice.domain.entities;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationEventType {

    private Long id;
    private String enumerator;
    private String emailSubject;
    private String emailTemplate;
    private String emailVariables;
    private LocalDateTime createdAt;

    public NotificationEventType() {
    }

    public NotificationEventType(
            Long id,
            String enumerator,
            String emailSubject,
            String emailTemplate,
            String emailVariables,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.enumerator = enumerator;
        this.emailSubject = emailSubject;
        this.emailTemplate = emailTemplate;
        this.emailVariables = emailVariables;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnumerator() {
        return this.enumerator;
    }

    public void setEnumerator(String enumerator) {
        this.enumerator = enumerator;
    }

    public String getEmailSubject() {
        return this.emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailTemplate() {
        return this.emailTemplate;
    }

    public void setEmailTemplate(String emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public String getEmailVariables() {
        return this.emailVariables;
    }

    public void setEmailVariables(String emailVariables) {
        this.emailVariables = emailVariables;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getEmailVariableNames() {
        return List.of(this.emailVariables.split(","));
    }
}
