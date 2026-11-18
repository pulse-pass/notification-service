package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.domain.repositories.EmailMessageStatusRepository;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageStatusEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class EmailMessageStatusRepositoryImpl implements EmailMessageStatusRepository {

    private final JpaEmailMessageStatusRepository jpaRepository;

    public EmailMessageStatusRepositoryImpl(JpaEmailMessageStatusRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<EmailMessageStatus> findByEnumerator(String enumerator) {
        return jpaRepository.findByEnumerator(enumerator).map(EmailMessageStatusEntity::toDomain);
    }
}
