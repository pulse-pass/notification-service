package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.repositories.NotificationEventRepository;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationEventRepositoryImpl implements NotificationEventRepository {

    private final JpaNotificationEventRepository jpaRepository;

    public NotificationEventRepositoryImpl(JpaNotificationEventRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NotificationEvent save(NotificationEvent notificationEvent) {
        NotificationEventEntity saved = jpaRepository.save(NotificationEventEntity.fromDomain(notificationEvent));
        return saved.toDomain();
    }

    @Override
    public Optional<NotificationEvent> findForUpdateByNotificationEventKey(String notificationEventKey) {
        return jpaRepository.findForUpdateByNotificationEventKey(notificationEventKey)
                .map(NotificationEventEntity::toDomain);
    }
}
