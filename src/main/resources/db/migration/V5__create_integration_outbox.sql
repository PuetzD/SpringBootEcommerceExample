CREATE TABLE integration_outbox (
    event_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_key VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    CONSTRAINT pk_integration_outbox PRIMARY KEY (event_id),
    CONSTRAINT chk_integration_outbox_attempt_count CHECK (attempt_count >= 0)
);

CREATE INDEX ix_integration_outbox_unpublished
    ON integration_outbox (created_at)
    WHERE published_at IS NULL;

CREATE INDEX ix_integration_outbox_aggregate
    ON integration_outbox (aggregate_type, aggregate_key);
