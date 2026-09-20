package com.springbootecommerce.shophappens.account.application.port.in;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(AccountReference account) {
        super("Account not found: " + account.value());
    }
}
