package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventTypeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNotificationEventTypeRepository extends JpaRepository<NotificationEventTypeEntity, Long> {

    Optional<NotificationEventTypeEntity> findByEnumerator(String enumerator);
}
