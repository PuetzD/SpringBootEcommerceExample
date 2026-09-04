# ADR 0009: Cross-Context Commerce Integrity

## Status

Accepted as an engineering baseline; legal/accounting review remains required for retention and deletion behavior.

## Decision

Ordering owns accepted commercial snapshots. Catalog, Customer, Payment, Reservation, Shipment, Return, and Refund expose published operations and immutable records rather than shared persistence entities. PostgreSQL remains the synchronous consistency boundary only for local checkout facts; cross-context and provider progress is coordinated through authenticated idempotent commands/events.

## Rationale

Shared entities would couple lifecycle and retention rules. Local transactionality is valuable for quote acceptance, reservation creation, Order snapshotting, and outbox append, while remote work requires replayable asynchronous coordination.

## Consequences

Historical Orders remain readable after Catalog or Customer changes. Account closure must distinguish erasable profile data from legally retained financial records and must record any anonymization or legal hold.
