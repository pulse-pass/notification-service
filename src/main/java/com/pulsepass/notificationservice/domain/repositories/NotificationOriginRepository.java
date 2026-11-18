package com.pulsepass.notificationservice.domain.repositories;

import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import java.util.Optional;

public interface NotificationOriginRepository {

    Optional<NotificationOrigin> findByEnumerator(String enumerator);
}
