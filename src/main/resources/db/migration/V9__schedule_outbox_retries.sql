ALTER TABLE integration_outbox
    ADD COLUMN next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN quarantined_at TIMESTAMPTZ;

CREATE INDEX ix_integration_outbox_eligible
    ON integration_outbox (next_attempt_at, created_at, event_id)
    WHERE published_at IS NULL AND quarantined_at IS NULL;
