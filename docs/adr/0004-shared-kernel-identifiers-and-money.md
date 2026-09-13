# ADR-0004: Shared kernel identifiers and Money

## Title

Share stable identifier meanings and Money through the shared kernel.

## Status

Accepted

## Context

Several bounded contexts collaborate using Account, Customer, Product-family,
and sellable Product Variant identifiers, and Catalog and Ordering need the same stable monetary
meaning. Duplicating these value-object meanings in each context creates accidental differences and
makes context contracts harder to understand.

## Decision

The shared kernel contains only the stable `AccountId`, `CustomerId`,
`ProductId`, `ProductVariantId`, `Money`, and `Currency` meanings. Contexts may
use these immutable values in published contracts. Aggregates, commands,
exceptions, repositories, framework types, and context-local identifiers such
as `Sku`, `CategoryId`, `AddressId`, `CartId`, and `OrderId` remain owned by
their contexts and are not part of the shared kernel.

## Consequences

Cross-context contracts can share unambiguous identifiers and monetary values
without sharing persistence entities or domain behavior. The kernel must stay
small and stable; a value is added only when its meaning is genuinely the same
in every consuming context.

## Alternatives Considered

- Keep separate copies in every context: rejected because the identifier and
  Money meanings are semantically shared and duplicated invariants could drift.
- Share aggregates or persistence models: rejected because it couples context
  internals and bypasses published contracts.
