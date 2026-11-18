package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.domain.entities.EmailMessageAttempt;
import com.pulsepass.notificationservice.domain.repositories.EmailMessageAttemptRepository;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageAttemptEntity;
import org.springframework.stereotype.Repository;

@Repository
public class EmailMessageAttemptRepositoryImpl implements EmailMessageAttemptRepository {

    private final JpaEmailMessageAttemptRepository jpaRepository;

    public EmailMessageAttemptRepositoryImpl(JpaEmailMessageAttemptRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EmailMessageAttempt save(EmailMessageAttempt emailMessageAttempt) {
        EmailMessageAttemptEntity saved = jpaRepository.save(EmailMessageAttemptEntity.fromDomain(emailMessageAttempt));
        return saved.toDomain();
    }

    @Override
    public long countByEmailMessageId(Long emailMessageId) {
        return jpaRepository.countByEmailMessageId(emailMessageId);
    }
}
