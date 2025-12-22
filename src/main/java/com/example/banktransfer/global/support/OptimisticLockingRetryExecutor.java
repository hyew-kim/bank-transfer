package com.example.banktransfer.global.support;

import com.example.banktransfer.global.exception.ConcurrentModificationException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class OptimisticLockingRetryExecutor {
    private static final int MAX_RETRIES = 10;
    private static final long BASE_BACKOFF_MILLIS = 10L;

    private final TransactionTemplate transactionTemplate;

    public OptimisticLockingRetryExecutor(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void run(Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }

    public <T> T execute(Supplier<T> action) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return transactionTemplate.execute(status -> action.get());
            } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
                if (attempt == MAX_RETRIES) {
                    throw new ConcurrentModificationException();
                }
                try {
                    Thread.sleep(BASE_BACKOFF_MILLIS * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new ConcurrentModificationException();
                }
            }
        }

        throw new ConcurrentModificationException();
    }
}
