# Commerce Operating Model

**Status:** Portfolio engineering baseline approved on 2026-09-04. This is an extensibility target, not a production launch approval; legal, tax, finance, security, accessibility, and fulfillment review is deferred until a real selling operation is selected.

## Purpose and scope

This baseline defines the vocabulary, ownership, and seams needed to grow the learning application toward B2C physical-goods commerce without implementing the whole commerce platform. It deliberately chooses one selling jurisdiction, one currency, simple products, and one shipment per order as portfolio examples. Expansion requires a new decision and an updated bounded-context map.

The portfolio implementation stops after a small, demonstrable slice: stable immutable commercial records, explicit money/currency boundaries, and published ports that can later host quote, payment, reservation, shipping, fulfillment, and privacy workflows. Provider integrations, legal automation, and production operations are intentionally deferred.

## Commercial decisions

| Decision | Baseline |
|---|---|
| Selling market | Germany-only is the illustrative baseline. It is not a launch or tax decision. |
| Goods | Physical goods only. No digital delivery, subscriptions, marketplace sellers, or regulated goods. |
| Currency | EUR only. Amounts use two-decimal precision and explicit `EUR` currency metadata in new commercial records. |
| Display prices | Consumer-facing prices are gross and include German VAT. The checkout breakdown shows net tax basis and VAT separately. |
| Invoice | Invoice is a future immutable financial document, distinct from Order; invoice generation is deferred. |
| Catalog shape | Simple products: one Product equals one sellable SKU with its own price and stock. Variants are deferred. |
| Scale envelope | Future target: 100,000 active SKUs, 50 reads/second peak catalog traffic, and 10 orders/minute peak checkout traffic. The portfolio only establishes bounded-contract seams. |

The Germany/VAT and invoice assumptions require confirmation by a qualified tax adviser before live selling. Gross display prices and EUR are engineering defaults, not tax advice.

## Checkout policy

- Existing account checkout remains the portfolio behavior. Guest checkout remains a separately scoped extension.
- The Cart remains mutable intent. A future Checkout Quote will be a separate published concept, not a price field added to Cart.
- Future quotes are expected to be valid for 15 minutes and expire at the boundary; this is a target rule, not current behavior.
- A price, tax, shipping, address, or availability change invalidates the quote and requires explicit customer confirmation of a replacement quote.
- Future quote limits are 1–99 units per SKU and at most 100 distinct SKUs. Current Cart limits remain authoritative until that work package is implemented.
- Inventory reservation, abandoned-checkout expiry, and payment reconciliation are future workflows with separate lifecycles.
- The future payable amount must be immutable when accepted and snapshotted on Order; the current Order total remains merchandise-only until the quote package is implemented.

## Future payment seam

- A future Payment context may use Stripe Payments with hosted Checkout or provider-hosted Elements. No provider is integrated in the portfolio slice.
- The future contract must support provider-neutral methods, authorize/capture, provider-managed 3DS/SCA, signed idempotent webhooks, refunds, and reconciliation without accepting card data.
- Provider calls must occur after local quote/reservation commit and outside PostgreSQL transactions.

Stripe is a replaceable adapter choice. Provider-specific types and schemas stay outside Ordering and the Payment domain.

## Future shipping and tax seams

- A future Shipping capability will own zones, methods, rates, delivery promises, and address-validation adapters. One standard Germany example and one shipment per Order are documentation defaults only.
- A future Tax capability will own jurisdiction rules and record rate, tax basis, tax amount, and rule/version on Quote and Order. It must support mixed rates without placing tax rules in Catalog or Cart.
- Shipping and tax outages must fail explicitly; the system must not guess a payable amount.

Tax treatment requires qualified tax review. Provider outages produce an explicit checkout failure; the system must not guess a tax or shipping amount.

## Lifecycle decisions

### Checkout and Reservation

`DRAFT -> QUOTED -> INVENTORY_RESERVED -> PAYMENT_PENDING -> COMPLETED`.

`QUOTED -> EXPIRED`, `INVENTORY_RESERVED -> EXPIRED`, and any non-completed state may enter `FAILED` with a reason. Reservation release is idempotent. A completed reservation is consumed exactly once.

### Payment

`CREATED -> ACTION_REQUIRED -> AUTHORIZED -> CAPTURED`.

Failure and cancellation paths are `CREATED|ACTION_REQUIRED|AUTHORIZED -> FAILED|CANCELLED`; `CAPTURED -> REFUNDED` or `PARTIALLY_REFUNDED`. No transition may reduce a captured amount without a recorded provider refund.

### Order

`PLACED -> PAYMENT_PENDING -> PAID -> FULFILLING -> SHIPPED -> DELIVERED`.

Terminal or exceptional states are `CANCELLED`, `RETURN_PENDING`, `PARTIALLY_REFUNDED`, `REFUNDED`, and `FAILED`. Order status is not used as a substitute for Payment, Reservation, Shipment, Return, or Refund status.

### Shipment, cancellation, return, and refund

- Shipment: `READY -> PACKED -> SHIPPED -> DELIVERED`, with `CANCELLED` before shipment and `DELIVERY_FAILED` for carrier failure.
- Customer cancellation is allowed while the order is `PAID` or `FULFILLING` and before shipment, subject to operator review where packing has started.
- Return: `REQUESTED -> AUTHORIZED -> RECEIVED -> INSPECTED -> ACCEPTED|REJECTED`.
- Refund: `REQUESTED -> APPROVED -> SUBMITTED -> SUCCEEDED|FAILED`; only authorized fulfillment/support operators may approve, and finance owns reconciliation.
- Operators may not bypass transitions, alter immutable snapshots, refund more than captured, or release a reservation twice.

Every transition records actor/source, reason, timestamp, correlation ID, and prior/new state. Stale revisions are rejected.

## Future privacy and customer-rights direction

- The future privacy capability will inventory data by purpose, distinguish required processing from optional consent, and provide authenticated export, correction, closure, retention, anonymization, and legal-hold workflows.
- Illustrative engineering defaults are 30-day logs, 7-day exports, and 10-year financial/audit retention, but no retention period is treated as legally approved by this portfolio document.
- Payment credentials, secrets, and unnecessary personal data must never enter logs or historical Order snapshots.

These retention periods and lawful bases require qualified privacy/legal review for the actual merchant and jurisdictions.

## Review gates and downstream packages

Before a real launch, product, tax, finance, fulfillment, security, privacy/legal, accessibility, and operations owners must sign the assumptions above. The extensibility sequence is documented, but only the first slice is in portfolio scope:

1. Portfolio slice: currency-aware immutable commercial records and published extension ports.
2. Future package: quote and complete price breakdown.
3. Future package: inventory reservations and checkout state machine.
4. Future package: Payment context and provider integration.
5. Future package: Order operations, fulfillment, returns, and refunds.
6. Future package: reliable asynchronous delivery.
7. Future package: bounded catalog/storefront queries and merchandising foundations.
8. Future package: functional administration.
9. Future package: privacy and customer lifecycle.
10. Future package: accessibility, performance, and production operations.

Each future package requires its own approved design and TDD plan. No provider, tax, legal, or operational automation is implemented merely because it appears in this target model.
