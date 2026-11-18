package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.repositories.EmailMessageRepository;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class EmailMessageRepositoryImpl implements EmailMessageRepository {

    private final JpaEmailMessageRepository jpaRepository;

    public EmailMessageRepositoryImpl(JpaEmailMessageRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EmailMessage save(EmailMessage emailMessage) {
        EmailMessageEntity saved = jpaRepository.save(EmailMessageEntity.fromDomain(emailMessage));
        return saved.toDomain();
    }

    @Override
    public Optional<EmailMessage> findByNotificationEventId(Long notificationEventId) {
        return jpaRepository.findByNotificationEventId(notificationEventId).map(EmailMessageEntity::toDomain);
    }
}
