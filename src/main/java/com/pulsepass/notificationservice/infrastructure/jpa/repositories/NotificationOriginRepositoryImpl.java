package com.pulsepass.notificationservice.infrastructure.jpa.repositories;

import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.domain.repositories.NotificationOriginRepository;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationOriginEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationOriginRepositoryImpl implements NotificationOriginRepository {

    private final JpaNotificationOriginRepository jpaRepository;

    public NotificationOriginRepositoryImpl(JpaNotificationOriginRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<NotificationOrigin> findByEnumerator(String enumerator) {
        return jpaRepository.findByEnumerator(enumerator).map(NotificationOriginEntity::toDomain);
    }
}
