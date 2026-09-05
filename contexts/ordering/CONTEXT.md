# Ordering

Ordering converts a Customer's Cart into an immutable record of a purchase. It captures the commercial and postal facts that were accepted at checkout.

## Language

**Checkout**:
The all-or-nothing attempt to place an Order from a Customer's current Cart using selected shipping and billing Addresses and current Catalog facts.
_Avoid_: Payment, Cart submission

**Checkout ID**:
The stable identifier supplied for one Checkout attempt. Reusing it returns the original outcome instead of placing a duplicate Order.
_Avoid_: Order Number, payment token

**Order**:
The immutable record of a purchase accepted at Checkout. It belongs to one Customer and is identified by a stable Order Number.
_Avoid_: Cart, transaction, invoice

**Order Number**:
The stable, customer-visible business identifier for an Order.
_Avoid_: Database ID, checkout token

**Order Item**:
An immutable snapshot of one purchased Product, including its Product identifier, name, SKU, unit Price, Quantity, and line total.
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
The immutable payable EUR amount accepted at Checkout. It is the sum of merchandise, discounts, shipping, and tax in the accepted Price Breakdown.
_Avoid_: Cart total, current catalog total, recomputed historical total

**Price Breakdown**:
The immutable accepted calculation of merchandise, discounts, shipping, tax basis, tax, and payable total for one Checkout Quote.
_Avoid_: Cart total, invoice, provider response

**Checkout Quote**:
A short-lived, versioned offer containing authoritative line snapshots, normalized addresses, selected shipping, tax calculation, and a Price Breakdown. It must be explicitly accepted before an Order is created.
_Avoid_: Cart, estimate, Order

**Reservation**:
An expiring commitment of Catalog stock for one Checkout Quote. It is consumed once by a completed Order or released once on failure/expiry.
_Avoid_: Stock decrement, Order, availability flag

**Payment**:
The separately tracked provider-backed attempt to authorize and capture the accepted Order Total. Payment state does not replace Order state.
_Avoid_: Checkout, card data, paid Order

**Shipment**:
The fulfillment record for sending an Order to its Shipping Order Address, including carrier progress and tracking.
_Avoid_: Order, shipping charge, delivery promise

**Return**:
An authorized post-delivery request to send purchased goods back for inspection and disposition.
_Avoid_: Cancellation, Refund

**Refund**:
A recorded reversal of all or part of a captured Payment, linked to the applicable Order items and provider result.
_Avoid_: Return, discount, cancellation

**Placed Order**:
An Order accepted after every Product and Address passes Checkout and the purchase is recorded completely.
_Avoid_: Confirmed Order, paid Order

**Money**:
A non-negative EUR monetary amount with two-decimal precision. Ordering snapshots the currency, tax basis, tax, charges, and payable amount and never recalculates a historical Order.
_Avoid_: Decimal, implicit currency, recomputed total
