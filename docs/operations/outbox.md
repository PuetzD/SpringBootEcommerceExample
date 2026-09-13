# Integration outbox operations

The optional Kafka publisher provides at-least-once delivery. A retry keeps the original event ID
and payload, so consumers must deduplicate by `event-id`. Run only one publisher instance; the
current query does not lease rows between workers.

New checkouts append `ordering.order-placed.v1`. The stored event type is the Kafka topic and the
matching version is sent in headers. A process-local
in-flight guard prevents an asynchronous send from being selected again before its callback updates
the outbox row.

## Order confirmation email

When Kafka publishing is enabled, the order-confirmation consumer reads
`ordering.order-placed.v1` and sends the customer email through Spring Mail. The
event contains the customer recipient snapshot and immutable order contents; the
consumer does not query mutable Catalog or Customer Profile state.

The `order_confirmation_delivery` table uses the event ID as its durable key.
Transient mail failures are retried, and five failed attempts quarantine the
delivery. Inspect delivery state without exposing message payloads:

```sql
SELECT event_id, order_number, status, attempt_count, last_error,
       next_attempt_at, sent_at
FROM order_confirmation_delivery
WHERE status <> 'SENT'
ORDER BY next_attempt_at, created_at;
```

Plain SMTP has a small crash window after the provider accepts a message but
before the sent marker is committed; this workflow is at-least-once, not a
distributed exactly-once guarantee.

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

Restoring broker connectivity only makes non-quarantined due rows eligible. It does not release a
row quarantined after five failures; an operator must deliberately target that row after checking
its diagnostic and cause.

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
