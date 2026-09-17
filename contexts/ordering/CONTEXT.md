# Ordering

Ordering converts a Customer's Cart into an immutable record of a purchase. The current implementation captures reviewed merchandise and postal facts; it does not yet quote shipping or tax or process payment.

## Implemented language

**Checkout**:
The all-or-nothing attempt to place an Order from a Customer's reviewed Cart using selected shipping and billing Addresses and current Catalog purchase facts.
_Avoid_: Payment, Cart submission

**Checkout Review**:
A short-lived review of currently available Product Variant lines and their current Catalog prices. Checkout rejects a review that has expired or differs from the authoritative facts obtained while purchasing.
_Avoid_: Quote, reservation, price commitment

**Checkout ID**:
The stable identifier supplied for one Checkout attempt. Reusing it returns the original outcome instead of placing a duplicate Order.
_Avoid_: Order Number, payment token

**Order**:
The immutable record of a purchase accepted at Checkout. It belongs to one Customer and is identified by a stable Order Number.
_Avoid_: Cart, transaction, invoice

**Order Number**:
The stable, customer-visible, readable, sequence-backed business identifier for an Order.
_Avoid_: Database ID, checkout token

**Order Item**:
An immutable snapshot of one purchased Product Variant, including its variant and product-family identifiers, SKU, product name, unit Price, Quantity, and line total.
_Avoid_: Cart Item, product reference

**Order Address**:
An immutable snapshot of an Address accepted at Checkout. Later edits to the Customer's saved Address do not change it.
_Avoid_: Saved Address, Customer Address

**Shipping Order Address**:
The Order Address recording where the purchased goods are to be sent.
_Avoid_: Shipping Address, Delivery Address

**Billing Order Address**:
The Order Address recording where billing correspondence is to be directed.
_Avoid_: Billing Address, Invoice Address

**Order Total**:
The immutable merchandise total in EUR accepted at Checkout. It is the sum of the Order Item line totals; the current implementation has no discount, shipping, or tax components.
_Avoid_: Price Breakdown, current catalog total, recomputed historical total

**Placed Order**:
An Order accepted after every Product Variant and Address passes Checkout and the purchase is recorded completely.
_Avoid_: Confirmed Order, paid Order

**Order Confirmation Recipient**:
The Customer's name and Contact Email captured when an Order is placed, identifying where the order confirmation is intended to be sent. Later Customer Profile changes do not alter this recipient snapshot.
_Avoid_: current Customer Email, Account Email

**Money**:
A non-negative EUR monetary amount with two-decimal precision. Ordering snapshots merchandise prices and totals and never recalculates a historical Order.
_Avoid_: Decimal, implicit currency, recomputed total

## Planned language

The following terms describe proposed boundaries, not implemented checkout behavior. See
[ADR-0007](../../docs/adr/0007-commerce-operating-model-baseline.md) and
[ADR-0008](../../docs/adr/0008-payment-and-reservation-boundaries.md).

**Checkout Quote / Price Breakdown**:
A future versioned offer and accepted calculation that may include discounts, shipping, tax basis, tax, and payable total.

**Reservation**:
A future expiring stock commitment consumed by a completed Order or released on failure or expiry.

**Payment**:
A future provider-backed authorization/capture lifecycle separate from Order state.

**Shipment / Return / Refund**:
Future fulfillment and post-purchase lifecycles with their own state, idempotency, and published contracts.
