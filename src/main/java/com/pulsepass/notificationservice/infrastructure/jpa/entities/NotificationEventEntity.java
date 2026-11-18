package com.pulsepass.notificationservice.infrastructure.jpa.entities;

import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
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
@Table(name = "notification_event")
public class NotificationEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_event_key", nullable = false, unique = true, length = 36)
    private String notificationEventKey;

    @Column(name = "person_key", length = 36)
    private String personKey;

    @Column(name = "origin_key", nullable = false, length = 36)
    private String originKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_event_type_id", nullable = false)
    private NotificationEventTypeEntity notificationEventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_origin_id", nullable = false)
    private NotificationOriginEntity notificationOrigin;

    @Column(name = "data", nullable = false, columnDefinition = "json")
    private String data;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public NotificationEventEntity() {
    }

    public NotificationEventEntity(
            Long id,
            String notificationEventKey,
            String personKey,
            String originKey,
            NotificationEventTypeEntity notificationEventType,
            NotificationOriginEntity notificationOrigin,
            String data,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.notificationEventKey = notificationEventKey;
        this.personKey = personKey;
        this.originKey = originKey;
        this.notificationEventType = notificationEventType;
        this.notificationOrigin = notificationOrigin;
        this.data = data;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NotificationEventEntity fromDomain(NotificationEvent domain) {
        return new NotificationEventEntity(
                domain.getId(),
                domain.getNotificationEventKey(),
                domain.getPersonKey(),
                domain.getOriginKey(),
                NotificationEventTypeEntity.fromDomain(domain.getNotificationEventType()),
                NotificationOriginEntity.fromDomain(domain.getNotificationOrigin()),
                domain.getData(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public NotificationEvent toDomain() {
        return new NotificationEvent(
                this.id,
                this.notificationEventKey,
                this.personKey,
                this.originKey,
                this.notificationEventType.toDomain(),
                this.notificationOrigin.toDomain(),
                this.data,
                this.createdAt,
                this.updatedAt
        );
    }
}
