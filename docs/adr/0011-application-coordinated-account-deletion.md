# ADR-0011: Application-coordinated account deletion

## Status

Accepted

## Context

Account, Customer Profile, and Cart are separate bounded contexts. A database
foreign key must not make one context the persistence owner of another context's
state, while account deletion must remove current customer state and preserve
historical orders.

## Decision

The Account application service coordinates deletion in one modular-monolith
transaction. It verifies the Account, resolves the Customer through a
consumer-owned output port, removes the Customer Cart when present, requests
Customer Profile removal, and deletes the Account last. Customer Profile and
Cart own idempotent cleanup operations; adapters translate identifiers and
published input contracts without importing foreign persistence types.

Orders retain logical Customer identifiers and immutable snapshots. They are not
deleted during account removal.

## Consequences

An exception from any cleanup operation rolls back the Account deletion and
current state cleanup together. The workflow relies on a shared transaction
manager in the modular monolith. If contexts are independently deployed, a
process manager and durable compensation/idempotency protocol must replace this
transactional orchestration.

## Alternatives Considered

- Cross-context database foreign keys and cascades: rejected because they couple
  schema ownership and hide business sequencing.
- Deleting historical Orders: rejected because it destroys the purchase record.
- Independent asynchronous cleanup now: rejected because the current deployment
  can provide atomic deletion and does not yet have a process manager.
