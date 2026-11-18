package com.pulsepass.notificationservice.domain;

public interface UnitOfWork {

    void begin();

    void commit();

    void rollback();

    boolean isActive();
}
