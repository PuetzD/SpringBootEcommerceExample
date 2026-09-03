# ADR-0001: Shared identifier value objects

## Title

Promote stable shared identifier value objects into the shared kernel.

## Status

Accepted

## Context

Account, Customer, and Product identifiers are duplicated across contexts as
identical value-object wrappers. They represent the same stable external
concepts that the contexts collaborate on, similar to the already-shared
meaning of `Money`.

## Decision

The shared kernel contains the canonical `AccountId`, `CustomerId`, and
`ProductId` values. Context-local identifiers such as `Sku`, `CategoryId`,
`AddressId`, `CartId`, and `OrderId` remain owned by their respective contexts.

## Consequences

Cross-context references use one unambiguous immutable identifier type, making
published contracts easier to understand and avoiding accidental divergence.
These identifiers become shared API, so future representation changes affect
all consuming contexts.

## Alternatives Considered

- Keep every identifier local to its context: rejected because the three
  duplicated values have the same meaning and invariants.
- Promote all identifiers to the shared kernel: rejected because it would
  expose context-specific vocabulary and increase coupling unnecessarily.
