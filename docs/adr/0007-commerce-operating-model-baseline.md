# ADR 0007: Conservative B2C Commerce Operating Model

## Status

Accepted as a future engineering baseline; it is not the implemented checkout workflow. The
variant-deferral portion is superseded by
[ADR-0010](./0010-product-variants-and-sellable-identity.md). Qualified tax, legal, finance,
fulfillment, and security review remains a release prerequisite.

## Decision

The originally proposed production slice targeted German B2C physical-goods sales in EUR with
simple SKU-backed Products, account-required checkout, one standard shipment, provider-hosted
Stripe payment collection, 15-minute quotes and reservations, gross consumer prices, and immutable
commercial snapshots. Product families and Product Variants now replace the simple-Product part of
that proposal; payment, quotes, reservations, shipping, and tax remain future work.

## Rationale

These choices minimize unresolved jurisdiction, variant, fulfillment, and payment semantics while preserving explicit seams for later expansion. They keep external provider calls outside the PostgreSQL transaction and prevent Cart, Order, Payment, and Reservation from becoming one overloaded lifecycle.

## Consequences

A future live-commerce release under this baseline cannot sell outside Germany, support guest
checkout, split shipments, or silently continue when tax/shipping data is unavailable. Changing
those boundaries requires a new decision and compatibility plan.
