package com.pulsepass.notificationservice.unit.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.domain.UnitOfWork;
import com.pulsepass.notificationservice.infrastructure.kafka.OpenEntityManagerInRecordInterceptor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class OpenEntityManagerInRecordInterceptorTest {

    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private UnitOfWork unitOfWork;
    private Consumer<String, String> consumer;
    private ConsumerRecord<String, String> record;
    private OpenEntityManagerInRecordInterceptor interceptor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        entityManagerFactory = mock(EntityManagerFactory.class);
        entityManager = mock(EntityManager.class);
        unitOfWork = mock(UnitOfWork.class);
        consumer = mock(Consumer.class);
        record = new ConsumerRecord<>("notification_event", 0, 0L, "key", "value");
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.isOpen()).thenReturn(true);
        interceptor = new OpenEntityManagerInRecordInterceptor(entityManagerFactory, unitOfWork);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.hasResource(entityManagerFactory)) {
            TransactionSynchronizationManager.unbindResource(entityManagerFactory);
        }
    }

    @Test
    void intercept_record_bindsOpenEntityManagerAndReturnsSameRecord() {
        var result = interceptor.intercept(record, consumer);

        assertThat(result).isSameAs(record);
        var holder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(entityManagerFactory);
        assertThat(holder.getEntityManager()).isSameAs(entityManager);
    }

    @Test
    void afterRecord_activeUnitOfWork_rollsBackThenUnbindsAndClosesEntityManager() {
        interceptor.intercept(record, consumer);
        when(unitOfWork.isActive()).thenReturn(true);

        interceptor.afterRecord(record, consumer);

        InOrder order = inOrder(unitOfWork, entityManager);
        order.verify(unitOfWork).rollback();
        order.verify(entityManager).close();
        assertThat(TransactionSynchronizationManager.hasResource(entityManagerFactory)).isFalse();
    }

    @Test
    void afterRecord_inactiveUnitOfWork_onlyClosesEntityManager() {
        interceptor.intercept(record, consumer);
        when(unitOfWork.isActive()).thenReturn(false);

        interceptor.afterRecord(record, consumer);

        verify(unitOfWork, never()).rollback();
        verify(entityManager).close();
        assertThat(TransactionSynchronizationManager.hasResource(entityManagerFactory)).isFalse();
    }

    @Test
    void failure_activeUnitOfWork_rollsBackKeepingEntityManagerBound() {
        interceptor.intercept(record, consumer);
        when(unitOfWork.isActive()).thenReturn(true);

        interceptor.failure(record, new IllegalStateException("boom"), consumer);

        verify(unitOfWork).rollback();
        verify(entityManager, never()).close();
        assertThat(TransactionSynchronizationManager.hasResource(entityManagerFactory)).isTrue();
    }

    @Test
    void failure_inactiveUnitOfWork_doesNotRollBack() {
        interceptor.intercept(record, consumer);
        when(unitOfWork.isActive()).thenReturn(false);

        interceptor.failure(record, new IllegalStateException("boom"), consumer);

        verify(unitOfWork, never()).rollback();
    }
}
