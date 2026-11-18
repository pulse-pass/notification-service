package com.pulsepass.notificationservice.infrastructure.jpa.entities;

import com.pulsepass.notificationservice.domain.entities.EmailMessageHistory;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
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
@Table(name = "email_message_history")
public class EmailMessageHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_message_history_key", nullable = false, unique = true, length = 36)
    private String emailMessageHistoryKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_message_id", nullable = false)
    private EmailMessageEntity emailMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_status_id")
    private EmailMessageStatusEntity oldStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_status_id", nullable = false)
    private EmailMessageStatusEntity newStatus;

    @Column(name = "history_data", nullable = false, columnDefinition = "json")
    private String historyData;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public EmailMessageHistoryEntity() {
    }

    public EmailMessageHistoryEntity(
            Long id,
            String emailMessageHistoryKey,
            EmailMessageEntity emailMessage,
            EmailMessageStatusEntity oldStatus,
            EmailMessageStatusEntity newStatus,
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

    public static EmailMessageHistoryEntity fromDomain(EmailMessageHistory domain) {
        EmailMessageStatus oldStatus = domain.getOldStatus();
        return new EmailMessageHistoryEntity(
                domain.getId(),
                domain.getEmailMessageHistoryKey(),
                EmailMessageEntity.fromDomain(domain.getEmailMessage()),
                oldStatus == null ? null : EmailMessageStatusEntity.fromDomain(oldStatus),
                EmailMessageStatusEntity.fromDomain(domain.getNewStatus()),
                domain.getHistoryData(),
                domain.getCreatedAt()
        );
    }

    public EmailMessageHistory toDomain() {
        return new EmailMessageHistory(
                this.id,
                this.emailMessageHistoryKey,
                this.emailMessage.toDomain(),
                this.oldStatus == null ? null : this.oldStatus.toDomain(),
                this.newStatus.toDomain(),
                this.historyData,
                this.createdAt
        );
    }
}
