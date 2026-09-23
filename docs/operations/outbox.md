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
Each consumer attempt takes a five-minute lease. Transient SMTP failures use
this exact sequence: `1s`, `2s`, `4s`, `8s`, then quarantine on the fifth
failure.

An attempt returns one of three decisions: acquired, pending until a specific instant, or
terminal (`SENT`/`QUARANTINED`). A pending decision keeps the Kafka offset uncommitted and retries
without an attempt limit, waiting until `next_attempt_at` for `FAILED` or `claim_expires_at` for
`CLAIMED`. Restarting the consumer redelivers that same uncommitted record and honors the remaining
wait. Only successful processing or a terminal delivery permits the normal record acknowledgment.
Other exceptions retain Spring Kafka's default error handling. A persisted mail failure is
immediately redelivered into this pending path, preserving the existing retry sequence.

The notification listener disables automatic offset commits and uses record acknowledgments. Its
ten-minute `max.poll.interval.ms` covers the five-minute claim wait; the wait stops promptly when
the listener stops. Waiting holds up other records on that consumer. Keep this interval above the
maximum lease/retry wait plus processing time if those durations change. There is no recovery
scheduler or separate retry topic: recovery depends on the retained Kafka record and its retention.

Each acquisition or reclaim installs a new UUID `claim_token`. Both success and failure updates
must match the event ID, that token, and `CLAIMED` status in one atomic update. A stale worker
cannot change a replacement worker's outcome or live claim; a zero-row update raises a stale-claim
failure. If recording a mail failure loses ownership, that outcome failure is suppressed onto the
original mail exception. Token fencing protects database ownership, but cannot undo an SMTP send.

Inspect delivery state without exposing message payloads:

```sql
SELECT event_id, order_number, status, attempt_count, last_error,
       next_attempt_at, claim_expires_at, claim_token, sent_at
FROM order_confirmation_delivery
WHERE status <> 'SENT'
ORDER BY next_attempt_at, created_at;
```

`CLAIMED` rows with `claim_expires_at <= CURRENT_TIMESTAMP` are abandoned.
Inspect them directly when checking recovery:

```sql
SELECT event_id, order_number, attempt_count, last_error, claim_expires_at
FROM order_confirmation_delivery
WHERE status = 'CLAIMED'
  AND claim_expires_at <= CURRENT_TIMESTAMP
ORDER BY claim_expires_at;
```

A later delivery of the same event automatically reclaims an abandoned row;
do not manually reset an expired claim. Plain SMTP has a crash window after
the provider accepts a message but before the sent marker is committed, so the
workflow is at-least-once and the customer can receive a duplicate email. It
is not a distributed exactly-once guarantee.

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
