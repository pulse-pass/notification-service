package com.pulsepass.notificationservice.domain.repositories;

import com.pulsepass.notificationservice.domain.entities.EmailMessageHistory;

public interface EmailMessageHistoryRepository {

    EmailMessageHistory save(EmailMessageHistory emailMessageHistory);
}
