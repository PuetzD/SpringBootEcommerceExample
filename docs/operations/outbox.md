# Integration outbox operations

The optional Kafka publisher provides at-least-once delivery. A retry keeps the original event ID
and payload, so consumers must deduplicate by `event-id`. Run only one publisher instance; the
current query does not lease rows between workers.

## Inspect delivery state

The non-quarantined count includes both due work and retries scheduled for the future:

```sql
SELECT count(*), min(created_at)
FROM integration_outbox
WHERE published_at IS NULL AND quarantined_at IS NULL;
```

Check due work separately:

```sql
SELECT count(*), min(created_at)
FROM integration_outbox
WHERE published_at IS NULL
  AND quarantined_at IS NULL
  AND next_attempt_at <= CURRENT_TIMESTAMP;
```

Monitor quarantined work independently:

```sql
SELECT count(*), min(created_at)
FROM integration_outbox
WHERE published_at IS NULL AND quarantined_at IS NOT NULL;
```

Before replaying an event, inspect its bounded diagnostic and event metadata. Do not expose or copy
the payload while investigating because it can contain customer data.

```sql
SELECT event_id, event_type, created_at, attempt_count, last_error,
       next_attempt_at, quarantined_at
FROM integration_outbox
WHERE event_id = :event_id;
```

## Replay one quarantined event

Resolve the failure cause first. In a transaction, execute the following statement with a bound UUID
parameter—not string interpolation:

```sql
UPDATE integration_outbox
SET quarantined_at = NULL,
    attempt_count = 0,
    next_attempt_at = CURRENT_TIMESTAMP,
    last_error = NULL
WHERE event_id = :event_id
  AND published_at IS NULL
  AND quarantined_at IS NOT NULL;
```

Commit only when the affected-row count is exactly one. Otherwise roll back and investigate the
event state. Never mass-replay, delete, relabel, rewrite payloads, or generate a replacement event
ID. A timed-out Kafka send may already have reached the broker; replay therefore still depends on
consumer deduplication.
