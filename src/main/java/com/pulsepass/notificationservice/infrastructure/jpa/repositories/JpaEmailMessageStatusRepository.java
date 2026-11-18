package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageStatusEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailMessageStatusRepository extends JpaRepository<EmailMessageStatusEntity, Long> {

    Optional<EmailMessageStatusEntity> findByEnumerator(String enumerator);
}
