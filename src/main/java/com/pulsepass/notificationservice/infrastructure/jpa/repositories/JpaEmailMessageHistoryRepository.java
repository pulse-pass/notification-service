package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailMessageHistoryRepository extends JpaRepository<EmailMessageHistoryEntity, Long> {
}
