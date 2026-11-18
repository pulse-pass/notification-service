package com.pulsepass.notificationservice.infrastructure.jpa.entities;

import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_event_type")
public class NotificationEventTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enumerator", nullable = false, unique = true, length = 50)
    private String enumerator;

    @Column(name = "email_subject", nullable = false, length = 255)
    private String emailSubject;

    @Column(name = "email_template", nullable = false, length = 100)
    private String emailTemplate;

    @Column(name = "email_variables", nullable = false, length = 1056)
    private String emailVariables;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public NotificationEventTypeEntity() {
    }

    public NotificationEventTypeEntity(
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

    public static NotificationEventTypeEntity fromDomain(NotificationEventType domain) {
        return new NotificationEventTypeEntity(
                domain.getId(),
                domain.getEnumerator(),
                domain.getEmailSubject(),
                domain.getEmailTemplate(),
                domain.getEmailVariables(),
                domain.getCreatedAt()
        );
    }

    public NotificationEventType toDomain() {
        return new NotificationEventType(
                this.id,
                this.enumerator,
                this.emailSubject,
                this.emailTemplate,
                this.emailVariables,
                this.createdAt
        );
    }
}
