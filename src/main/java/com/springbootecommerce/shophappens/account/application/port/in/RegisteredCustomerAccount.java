package com.springbootecommerce.shophappens.account.application.port.in;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;

public record RegisteredCustomerAccount(AccountReference account, CustomerReference customer) {}
