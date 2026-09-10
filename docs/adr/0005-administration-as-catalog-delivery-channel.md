# ADR-0005: Administration as an inbound delivery channel

## Title

Model Administration as a protected inbound delivery channel, not a bounded context.

## Status

Accepted

## Amendment history

- **2026-08-31**: the original decision covered Catalog-owned Product and Category administration.
- **2026-09-11**: read-only Customer and Order administration was added through Customer Profile and
  Ordering contracts. This extends the delivery surface without changing Administration into a
  bounded context or transferring business ownership.

## Context

The administration surface delivers Product and Category management plus read-only Customer and
Order inspection. These capabilities remain part of the business models and persistence boundaries
owned by Catalog, Customer Profile, and Ordering; the HTTP and React-admin surface does not define
a sixth model.

## Decision

Administration is a protected inbound delivery adapter over provider-owned published input ports.
Catalog owns Product and Category administration contracts, validation, business policies,
optimistic revisions, and transactions. Customer Profile and Ordering own their read-only
administrative query contracts and facts. The adapter owns HTTP/React-admin transport,
authentication integration, CSRF handling, finite SPA forwarding, and representation mapping.
Direct database writes from the delivery channel are not allowed.

## Consequences

Each context remains the single owner of its behavior regardless of whether a request comes from a
public or administration surface. The Administration adapter may depend on published input ports,
but not on context domain objects, repositories, persistence adapters, or application services.
Adding a resource requires an owning context contract and an explicit SPA route, which adds mapping
and route-maintenance cost but keeps authorization and domain ownership visible.

## Alternatives Considered

- Create a sixth Administration bounded context: rejected because it would duplicate Product,
  Category, Customer, and Order language and split ownership of the same state.
- Allow direct database writes from Administration: rejected because it would
  bypass owning-context policies, optimistic concurrency, and transaction boundaries.
