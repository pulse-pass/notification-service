package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailMessageRepository extends JpaRepository<EmailMessageEntity, Long> {

    Optional<EmailMessageEntity> findByNotificationEventId(Long notificationEventId);
}
