package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface JpaNotificationEventRepository extends JpaRepository<NotificationEventEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<NotificationEventEntity> findForUpdateByNotificationEventKey(String notificationEventKey);
}
