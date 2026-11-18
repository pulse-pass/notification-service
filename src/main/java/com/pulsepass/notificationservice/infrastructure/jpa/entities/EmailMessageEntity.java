package com.pulsepass.notificationservice.infrastructure.jpa.entities;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_message")
public class EmailMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_message_key", nullable = false, unique = true, length = 36)
    private String emailMessageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_event_id", nullable = false)
    private NotificationEventEntity notificationEvent;

    @Column(name = "external_message_key", length = 36)
    private String externalMessageKey;

    @Column(name = "email_data", nullable = false, columnDefinition = "json")
    private String emailData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_message_status_id", nullable = false)
    private EmailMessageStatusEntity emailMessageStatus;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public EmailMessageEntity() {
    }

    public EmailMessageEntity(
            Long id,
            String emailMessageKey,
            NotificationEventEntity notificationEvent,
            String externalMessageKey,
            String emailData,
            EmailMessageStatusEntity emailMessageStatus,
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

    public static EmailMessageEntity fromDomain(EmailMessage domain) {
        return new EmailMessageEntity(
                domain.getId(),
                domain.getEmailMessageKey(),
                NotificationEventEntity.fromDomain(domain.getNotificationEvent()),
                domain.getExternalMessageKey(),
                domain.getEmailData(),
                EmailMessageStatusEntity.fromDomain(domain.getEmailMessageStatus()),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public EmailMessage toDomain() {
        return new EmailMessage(
                this.id,
                this.emailMessageKey,
                this.notificationEvent.toDomain(),
                this.externalMessageKey,
                this.emailData,
                this.emailMessageStatus.toDomain(),
                this.createdAt,
                this.updatedAt
        );
    }
}
