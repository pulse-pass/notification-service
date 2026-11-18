package com.pulsepass.notificationservice.domain.repositories;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import java.util.Optional;

public interface EmailMessageRepository {

    EmailMessage save(EmailMessage emailMessage);

    Optional<EmailMessage> findByNotificationEventId(Long notificationEventId);
}
