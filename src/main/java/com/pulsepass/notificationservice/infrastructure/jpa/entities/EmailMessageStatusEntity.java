package com.pulsepass.notificationservice.infrastructure.jpa.entities;

import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_message_status")
public class EmailMessageStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enumerator", nullable = false, unique = true, length = 50)
    private String enumerator;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public EmailMessageStatusEntity() {
    }

    public EmailMessageStatusEntity(Long id, String enumerator, LocalDateTime createdAt) {
        this.id = id;
        this.enumerator = enumerator;
        this.createdAt = createdAt;
    }

    public static EmailMessageStatusEntity fromDomain(EmailMessageStatus domain) {
        return new EmailMessageStatusEntity(domain.getId(), domain.getEnumerator(), domain.getCreatedAt());
    }

    public EmailMessageStatus toDomain() {
        return new EmailMessageStatus(this.id, this.enumerator, this.createdAt);
    }
}
