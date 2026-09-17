CREATE TABLE order_confirmation_delivery (
    event_id UUID NOT NULL,
    order_number VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_error VARCHAR(200),
    next_attempt_at TIMESTAMPTZ NOT NULL,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_order_confirmation_delivery PRIMARY KEY (event_id),
    CONSTRAINT chk_order_confirmation_delivery_status
        CHECK (status IN ('CLAIMED', 'SENT', 'FAILED', 'QUARANTINED')),
    CONSTRAINT chk_order_confirmation_delivery_attempt_count CHECK (attempt_count >= 0)
);

CREATE INDEX ix_order_confirmation_delivery_retry
    ON order_confirmation_delivery (next_attempt_at, created_at, event_id)
    WHERE status = 'FAILED';
