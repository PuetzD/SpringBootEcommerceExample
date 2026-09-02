# ADR-0002: Administration as a Catalog delivery channel

## Title

Model Administration as a delivery channel owned by Catalog.

## Status

Accepted

## Context

Product and Category administration changes Catalog state and policies. The
administration surface needs HTTP and React-Admin delivery adapters, but those
surfaces do not define a separate business model or persistence boundary.

## Decision

Administration is a protected inbound delivery adapter over Catalog's
published input ports. Catalog owns administration contracts, validation and
business policies, optimistic revisions, and transactions. The adapter owns
HTTP/React-Admin transport, authentication integration, CSRF handling, and
representation mapping. Direct database writes from the delivery channel are
not allowed.

## Consequences

Catalog remains the single owner of Product and Category behavior regardless
of whether a request comes from the storefront or administration. The
administration adapter may depend on Catalog's published input ports, but not
on Catalog domain objects, repositories, persistence adapters, or application
services. Adding another administration resource requires an explicit Catalog
contract rather than a parallel delivery-layer model.

## Alternatives Considered

- Create a sixth Administration bounded context: rejected because it would
  duplicate Catalog language and split ownership of the same state.
- Allow direct database writes from Administration: rejected because it would
  bypass Catalog policies, optimistic concurrency, and transaction boundaries.
