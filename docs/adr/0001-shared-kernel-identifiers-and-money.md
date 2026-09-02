# ADR-0001: Shared kernel identifiers and Money

## Title

Share stable identifier meanings and Money through the shared kernel.

## Status

Accepted

## Context

Several bounded contexts collaborate using Account, Customer, and Product
identifiers, and Catalog and Ordering need the same stable monetary meaning.
Duplicating these value-object meanings in each context creates accidental
differences and makes context contracts harder to understand.

## Decision

The shared kernel contains only the stable `AccountId`, `CustomerId`,
`ProductId`, and `Money` meanings. Contexts may use these immutable values in
published contracts. Aggregates, commands, exceptions, repositories, and
framework types remain owned by their contexts and are not part of the shared
kernel.

## Consequences

Cross-context contracts can share unambiguous identifiers and monetary values
without sharing persistence entities or domain behavior. The kernel must stay
small and stable; a value is added only when its meaning is genuinely the same
in every consuming context.

## Alternatives Considered

- Keep separate copies in every context: rejected because the identifier and
  Money meanings are byte-identical and semantically shared.
- Share aggregates or persistence models: rejected because it couples context
  internals and bypasses published contracts.
