package com.pulsepass.notificationservice.domain;

import java.time.Duration;

public interface Cache {

    boolean acquireLock(String key, Duration ttl);

    void delete(String key);
}
