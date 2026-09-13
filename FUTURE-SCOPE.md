# Future scope

This file lists planned capabilities, not promises about current behavior. The implemented baseline
already includes Product families and Product Variants, variant-aware Cart and Ordering, a
merchandise Checkout Review, immutable Orders, and transactional-outbox publishing with targeted
operator replay. Those capabilities are documented in the [context map](CONTEXT_MAP.md) and
[outbox runbook](docs/operations/outbox.md).

## Pricing, checkout, and payment

- A versioned commercial Quote / Price Breakdown with discounts, shipping, tax basis, tax, and
  payable total; the current Checkout Review covers merchandise facts only
- Payment and Payment Method lifecycles, including provider authorization/capture and authenticated,
  idempotent webhook handling
- Expiring stock reservations and release/reconciliation behavior
- Shipping method selection and rate calculation
- Tax calculation and VAT rules
- Discounts, Coupons, and Promotions

## Fulfillment and post-purchase

- Shipment and delivery tracking as a lifecycle separate from Order
- Fulfillment workflows and split shipments
- Returns and Refunds with explicit state and idempotency
- Invoice as an accounting document, distinct from the billing Order Address snapshot
- Customer notifications such as email or push delivery

## Catalog and supply

- Warehouse-owned Inventory and multiple stock locations; current Stock Quantity is Catalog-owned
  shop-wide availability
- Suppliers and replenishment
- Nested Categories; current Categories are flat
- Full-text search or a dedicated search engine
- Wishlists and Product reviews/ratings

## Identity, privacy, and abuse protection

- Account recovery and credential-reset workflows
- Authentication abuse protection such as rate limiting, lockout policy, and credential-stuffing
  defenses
- More granular administration roles beyond the current Customer/Administrator split
- Audit logging
- Automated privacy retention, erasure, anonymization, and legal-hold workflows

## Integration and operations

- Kafka consumers plus inbox/processed-event deduplication; none are currently implemented
- Consumer dead-letter and replay workflows
- Multi-publisher outbox leasing/claiming; the current runbook permits one publisher instance
- Delivery metrics, alerts, dashboards, and trace correlation for checkout and event publication
- Broader administration workflows beyond Product/Variant and Category management and read-only
  Order/Customer inspection

## B2B and market expansion

- Customer Company accounts and business purchasing roles
- VAT IDs on addresses and jurisdiction-specific Tax Rules
- Currencies and markets beyond the current EUR/German B2C engineering baseline

Event sourcing remains intentionally excluded as a persistence pattern. Future asynchronous
workflows should build on the existing outbox without making Kafka or remote providers a synchronous
checkout dependency. Proposed payment, reservation, and cross-context boundaries are recorded in
[ADR-0007](docs/adr/0007-commerce-operating-model-baseline.md),
[ADR-0008](docs/adr/0008-payment-and-reservation-boundaries.md), and
[ADR-0009](docs/adr/0009-cross-context-commerce-integrity.md).
