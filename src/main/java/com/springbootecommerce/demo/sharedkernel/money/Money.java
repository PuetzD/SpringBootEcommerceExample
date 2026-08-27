package com.springbootecommerce.demo.sharedkernel.money;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Embeddable
public class Money {
    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    protected Money() {}

    public Money(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
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

    public Money add(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money multiply(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)));
    }

    public BigDecimal amount() {
        return amount;
    }

    @Override
    public boolean equals(Object value) {
        return value instanceof Money other && amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }
}
