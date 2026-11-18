package com.pulsepass.notificationservice.infrastructure.jpa.entities;

import com.pulsepass.notificationservice.domain.entities.EmailMessageAttempt;
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
@Table(name = "email_message_attempt")
public class EmailMessageAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_message_attempt_key", nullable = false, unique = true, length = 36)
    private String emailMessageAttemptKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_message_id", nullable = false)
    private EmailMessageEntity emailMessage;

    @Column(name = "error_code", length = 8)
    private String errorCode;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public EmailMessageAttemptEntity() {
    }

    public EmailMessageAttemptEntity(
            Long id,
            String emailMessageAttemptKey,
            EmailMessageEntity emailMessage,
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

    public static EmailMessageAttemptEntity fromDomain(EmailMessageAttempt domain) {
        return new EmailMessageAttemptEntity(
                domain.getId(),
                domain.getEmailMessageAttemptKey(),
                EmailMessageEntity.fromDomain(domain.getEmailMessage()),
                domain.getErrorCode(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public EmailMessageAttempt toDomain() {
        return new EmailMessageAttempt(
                this.id,
                this.emailMessageAttemptKey,
                this.emailMessage.toDomain(),
                this.errorCode,
                this.createdAt,
                this.updatedAt
        );
    }
}
