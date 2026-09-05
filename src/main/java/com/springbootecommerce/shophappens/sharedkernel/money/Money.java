package com.springbootecommerce.shophappens.sharedkernel.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount) {
        this(amount, Currency.EUR);
    }

    public Money(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "amount");
        this.currency = Objects.requireNonNull(currency, "currency");
        try {
            this.amount = amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Money must use at most two decimal places", exception);
        }
        if (this.amount.signum() < 0) {
            throw new IllegalArgumentException("Money must not be negative");
        }
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other");
        if (currency != other.currency) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    @Override
    public boolean equals(Object value) {
        return value instanceof Money other
                && amount.compareTo(other.amount) == 0
                && currency == other.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
}
