package com.pulsepass.notificationservice.unit.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.infrastructure.jpa.JpaUnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

class JpaUnitOfWorkTest {

    private PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;
    private JpaUnitOfWork unitOfWork;

    @BeforeEach
    void setUp() {
        transactionManager = mock(PlatformTransactionManager.class);
        transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(transactionStatus);
        unitOfWork = new JpaUnitOfWork(transactionManager);
    }

    @Test
    void isActive_beforeBegin_returnsFalse() {
        assertThat(unitOfWork.isActive()).isFalse();
    }

    @Test
    void begin_noActiveTransaction_opensTransaction() {
        unitOfWork.begin();

        assertThat(unitOfWork.isActive()).isTrue();
        verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    }

    @Test
    void begin_alreadyActive_keepsTheSameTransaction() {
        unitOfWork.begin();
        unitOfWork.begin();

        assertThat(unitOfWork.isActive()).isTrue();
        verify(transactionManager, times(1)).getTransaction(any(TransactionDefinition.class));
    }

    @Test
    void commit_activeTransaction_commitsAndDeactivates() {
        unitOfWork.begin();

        unitOfWork.commit();

        assertThat(unitOfWork.isActive()).isFalse();
        verify(transactionManager).commit(transactionStatus);
    }

    @Test
    void commit_noActiveTransaction_throwsIllegalState() {
        assertThatThrownBy(() -> unitOfWork.commit()).isInstanceOf(IllegalStateException.class);

        verify(transactionManager, never()).commit(any());
    }

    @Test
    void rollback_activeTransaction_rollsBackAndDeactivates() {
        unitOfWork.begin();

        unitOfWork.rollback();

        assertThat(unitOfWork.isActive()).isFalse();
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void rollback_noActiveTransaction_throwsIllegalState() {
        assertThatThrownBy(() -> unitOfWork.rollback()).isInstanceOf(IllegalStateException.class);

        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void isActive_completedTransaction_returnsFalse() {
        unitOfWork.begin();
        when(transactionStatus.isCompleted()).thenReturn(true);

        assertThat(unitOfWork.isActive()).isFalse();
    }

    @Test
    void begin_afterCompletedTransaction_opensNewTransaction() {
        unitOfWork.begin();
        when(transactionStatus.isCompleted()).thenReturn(true);

        unitOfWork.begin();

        verify(transactionManager, times(2)).getTransaction(any(TransactionDefinition.class));
    }

    @Test
    void isActive_otherThread_returnsFalse() throws InterruptedException {
        unitOfWork.begin();
        var result = new boolean[] {true};

        var thread = new Thread(() -> result[0] = unitOfWork.isActive());
        thread.start();
        thread.join();

        assertThat(result[0]).isFalse();
        assertThat(unitOfWork.isActive()).isTrue();
    }
}
