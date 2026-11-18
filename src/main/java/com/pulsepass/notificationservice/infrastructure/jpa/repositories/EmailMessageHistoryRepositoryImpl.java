package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.domain.entities.EmailMessageHistory;
import com.pulsepass.notificationservice.domain.repositories.EmailMessageHistoryRepository;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageHistoryEntity;
import org.springframework.stereotype.Repository;

@Repository
public class EmailMessageHistoryRepositoryImpl implements EmailMessageHistoryRepository {

    private final JpaEmailMessageHistoryRepository jpaRepository;

    public EmailMessageHistoryRepositoryImpl(JpaEmailMessageHistoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EmailMessageHistory save(EmailMessageHistory emailMessageHistory) {
        EmailMessageHistoryEntity saved = jpaRepository.save(EmailMessageHistoryEntity.fromDomain(emailMessageHistory));
        return saved.toDomain();
    }
}
