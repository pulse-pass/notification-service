package com.pulsepass.notificationservice.infrastructure.jpa.entities;

import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_origin")
public class NotificationOriginEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enumerator", nullable = false, unique = true, length = 50)
    private String enumerator;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public NotificationOriginEntity() {
    }

    public NotificationOriginEntity(Long id, String enumerator, LocalDateTime createdAt) {
        this.id = id;
        this.enumerator = enumerator;
        this.createdAt = createdAt;
    }

    public static NotificationOriginEntity fromDomain(NotificationOrigin domain) {
        return new NotificationOriginEntity(domain.getId(), domain.getEnumerator(), domain.getCreatedAt());
    }

    public NotificationOrigin toDomain() {
        return new NotificationOrigin(this.id, this.enumerator, this.createdAt);
    }
}
