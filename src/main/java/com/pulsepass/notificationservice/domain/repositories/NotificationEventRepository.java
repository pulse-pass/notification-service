package com.pulsepass.notificationservice.domain.repositories;

import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import java.util.Optional;

public interface NotificationEventRepository {

    NotificationEvent save(NotificationEvent notificationEvent);

    Optional<NotificationEvent> findForUpdateByNotificationEventKey(String notificationEventKey);
}
