package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationOriginEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNotificationOriginRepository extends JpaRepository<NotificationOriginEntity, Long> {

    Optional<NotificationOriginEntity> findByEnumerator(String enumerator);
}
