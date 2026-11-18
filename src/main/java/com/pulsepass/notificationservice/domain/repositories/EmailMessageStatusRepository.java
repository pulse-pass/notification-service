package com.pulsepass.notificationservice.domain.repositories;

import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import java.util.Optional;

public interface EmailMessageStatusRepository {

    Optional<EmailMessageStatus> findByEnumerator(String enumerator);
}
