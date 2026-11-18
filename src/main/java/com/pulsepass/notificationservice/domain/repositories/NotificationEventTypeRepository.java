package com.pulsepass.notificationservice.domain.repositories;

import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import java.util.Optional;

public interface NotificationEventTypeRepository {

    Optional<NotificationEventType> findByEnumerator(String enumerator);
}
