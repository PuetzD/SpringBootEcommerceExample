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
The Money sum of all Order Item line totals.
_Avoid_: Cart total, payment amount

**Placed Order**:
An Order accepted after every Product and Address passes Checkout and the purchase is recorded completely.
_Avoid_: Confirmed Order, paid Order

**Money**:
A non-negative monetary amount with two-decimal precision. Ordering uses the same implicit-currency meaning of Money as Catalog.
_Avoid_: Decimal, currency-specific Money
