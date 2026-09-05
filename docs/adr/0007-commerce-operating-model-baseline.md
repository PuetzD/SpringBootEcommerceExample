# ADR 0007: Conservative B2C Commerce Operating Model

## Status

Accepted as an engineering baseline; qualified tax, legal, finance, fulfillment, and security review remains a release prerequisite.

## Decision

The first production slice targets German B2C physical-goods sales in EUR with simple SKU-backed Products, account-required checkout, one standard shipment, provider-hosted Stripe payment collection, 15-minute quotes and reservations, gross consumer prices, and immutable commercial snapshots.

## Rationale

These choices minimize unresolved jurisdiction, variant, fulfillment, and payment semantics while preserving explicit seams for later expansion. They keep external provider calls outside the PostgreSQL transaction and prevent Cart, Order, Payment, and Reservation from becoming one overloaded lifecycle.

## Consequences

The first release cannot sell outside Germany, support guest checkout, represent variants, split shipments, or silently continue when tax/shipping data is unavailable. Changing those boundaries requires a new decision and compatibility plan.
