package com.pulsepass.notificationservice.infrastructure.jpa;

import com.pulsepass.notificationservice.domain.UnitOfWork;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
public class JpaUnitOfWork implements UnitOfWork {

    private final PlatformTransactionManager transactionManager;
    private final ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<>();

    public JpaUnitOfWork(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void begin() {
        if (isActive()) {
            return;
        }
        var definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        this.transactionStatus.set(transactionManager.getTransaction(definition));
    }

    @Override
    public void commit() {
        if (!isActive()) {
            throw new IllegalStateException("No active transaction to commit");
        }
        var status = transactionStatus.get();
        transactionStatus.remove();
        transactionManager.commit(status);
    }

    @Override
    public void rollback() {
        if (!isActive()) {
            throw new IllegalStateException("No active transaction to rollback");
        }
        var status = transactionStatus.get();
        transactionStatus.remove();
        transactionManager.rollback(status);
    }

    @Override
    public boolean isActive() {
        var status = transactionStatus.get();
        return status != null && !status.isCompleted();
    }
}
