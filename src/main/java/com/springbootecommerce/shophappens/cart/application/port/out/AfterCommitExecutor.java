package com.springbootecommerce.shophappens.cart.application.port.out;

public interface AfterCommitExecutor {
    void execute(Runnable action);
}
