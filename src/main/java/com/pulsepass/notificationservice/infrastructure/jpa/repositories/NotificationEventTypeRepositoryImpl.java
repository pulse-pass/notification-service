package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.repositories.NotificationEventTypeRepository;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventTypeEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationEventTypeRepositoryImpl implements NotificationEventTypeRepository {

    private final JpaNotificationEventTypeRepository jpaRepository;

    public NotificationEventTypeRepositoryImpl(JpaNotificationEventTypeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<NotificationEventType> findByEnumerator(String enumerator) {
        return jpaRepository.findByEnumerator(enumerator).map(NotificationEventTypeEntity::toDomain);
    }
}
