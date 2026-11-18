package com.pulsepass.notificationservice.infrastructure.kafka;

import com.pulsepass.notificationservice.domain.UnitOfWork;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class OpenEntityManagerInRecordInterceptor implements RecordInterceptor<String, String> {

    private final EntityManagerFactory entityManagerFactory;
    private final UnitOfWork unitOfWork;

    public OpenEntityManagerInRecordInterceptor(EntityManagerFactory entityManagerFactory, UnitOfWork unitOfWork) {
        this.entityManagerFactory = entityManagerFactory;
        this.unitOfWork = unitOfWork;
    }

    private void rollbackIfActive() {
        if (unitOfWork.isActive()) {
            unitOfWork.rollback();
        }
    }

    @Override
    public ConsumerRecord<String, String> intercept(ConsumerRecord<String, String> record,
            Consumer<String, String> consumer) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TransactionSynchronizationManager.bindResource(entityManagerFactory, new EntityManagerHolder(entityManager));
        return record;
    }

    @Override
    public void failure(ConsumerRecord<String, String> record, Exception exception,
            Consumer<String, String> consumer) {
        rollbackIfActive();
    }

    @Override
    public void afterRecord(ConsumerRecord<String, String> record, Consumer<String, String> consumer) {
        rollbackIfActive();
        EntityManagerHolder holder = (EntityManagerHolder) TransactionSynchronizationManager
                .unbindResource(entityManagerFactory);
        EntityManagerFactoryUtils.closeEntityManager(holder.getEntityManager());
    }
}
