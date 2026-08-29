package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import com.springbootecommerce.shophappens.cart.application.port.out.AfterCommitExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
class SpringAfterCommitExecutor implements AfterCommitExecutor {
    @Override
    public void execute(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "AfterCommitExecutor.execute requires an active transaction");
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                });
    }
}