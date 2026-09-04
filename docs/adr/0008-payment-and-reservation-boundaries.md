# ADR 0008: Payment and Inventory Reservation Boundaries

## Status

Accepted as an engineering baseline; provider and finance review remains required.

## Decision

Payment and Inventory Reservation are separate lifecycles. Checkout commits a quote and reservation locally, then starts provider payment work after commit. Payment webhooks are authenticated and idempotent, and reservation expiry/release is independent of browser or provider callbacks.

## Rationale

A database transaction cannot safely span a remote payment provider or hold product locks while waiting for network work. Separate state machines allow retries, reconciliation, expiry, and recovery without double charging or overselling.

## Consequences

The system must persist idempotency keys, provider references, reservation IDs, webhook inbox records, and transition history. A successful browser return is never sufficient evidence of payment; provider outcomes drive completion.
