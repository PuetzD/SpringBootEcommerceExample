# ADR-0003: Account and customer data lifecycle

## Title

Define account deletion ownership and historical order retention.

## Status

Accepted

## Context

Account deletion affects customer profile data, addresses, carts, and existing
orders. The system also needs clear transaction ownership for scheduled outbox
processing.

## Decision

The account is the identity owner. Deleting an account cascades to its customer
profile, addresses, and customer cart through database foreign keys. Orders
retain their customer ID and immutable address and product snapshots; historical
orders are not deleted as a side effect of account removal.

Application services own business transactions. The scheduled outbox publisher
reads pending events and delegates status mutations to `OutboxStatusService`,
whose application operations own their transactions. Persistence adapters retain
only the transaction semantics required by independently callable persistence
operations.

## Consequences

Account removal cleans up current customer-owned data while preserving the
historical information needed to view past orders. Transaction boundaries remain
at the application-operation level for both request-driven and scheduled work.

## Alternatives Considered

- Cascade account deletion to orders: rejected because it would destroy
  historical order records.
- Let adapters own the business transaction: rejected because transaction
  ownership would vary by delivery mechanism and bypass application semantics.
