package com.springbootecommerce.shophappens.account.application;

import com.springbootecommerce.shophappens.account.domain.model.Email;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(Email email) {
        super("An account for email " + email.value() + " already exists");
    }
}
