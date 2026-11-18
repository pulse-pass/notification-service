package com.pulsepass.notificationservice.domain.repositories;

import com.pulsepass.notificationservice.domain.entities.EmailMessageAttempt;

public interface EmailMessageAttemptRepository {

    EmailMessageAttempt save(EmailMessageAttempt emailMessageAttempt);

    long countByEmailMessageId(Long emailMessageId);
}
