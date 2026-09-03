# ADR-0002: Cross-context contract ownership

## Title

Keep published contracts owned by their defining context.

## Status

Accepted

## Context

Bounded contexts need to exchange references and query results without making
another context's contracts, domain model, or persistence types into an
accidental integration API.

## Decision

Published application contracts use shared-kernel identity values for
cross-context references. Query results are immutable records owned by the
consuming context. Adapters translate between a published context contract and
the consuming context's port.

## Consequences

Each context owns the shape and meaning of its published contracts. Architecture
tests enforce these boundaries so contract dependencies and domain exposure fail
explicitly rather than becoming accidental coupling.

## Alternatives Considered

- Allow one context's published contracts to depend on another context's
  contracts or domain model: rejected because it transfers ownership and
  couples the bounded contexts.
- Share persistence types in published contracts: rejected because it exposes
  infrastructure and bypasses context boundaries.
