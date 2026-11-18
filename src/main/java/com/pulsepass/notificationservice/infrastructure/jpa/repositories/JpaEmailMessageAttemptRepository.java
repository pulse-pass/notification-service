package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailMessageAttemptRepository extends JpaRepository<EmailMessageAttemptEntity, Long> {

    long countByEmailMessageId(Long emailMessageId);
}
