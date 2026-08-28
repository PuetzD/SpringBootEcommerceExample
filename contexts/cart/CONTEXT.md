# Cart

Cart maintains a Customer's or anonymous visitor's current, mutable shopping selection before checkout. It records intent, not a price or stock commitment.

## Language

**Cart**:
An active shopping selection associated with either a Customer or an anonymous browser session. A Cart contains at most one Cart Item for each selected Product.
_Avoid_: Basket, bag, order

**Customer Cart**:
The single persistent Cart owned by a Customer. It remains available across authenticated sessions.
_Avoid_: Account Cart, saved Cart

**Guest Cart**:
A temporary Cart associated with an anonymous browser session through an opaque Guest Cart ID.
_Avoid_: Anonymous Cart, session Cart

**Guest Cart ID**:
The opaque identifier that associates an anonymous browser session with its Guest Cart without representing a Customer identity.
_Avoid_: Customer ID, session ID

**Cart Merge**:
The transfer of a Guest Cart into a Customer Cart after sign-in. Quantities are added for matching Products, and the Guest Cart is discarded after the Customer Cart is persisted.
_Avoid_: Cart replacement, Cart synchronization

**Cart Item**:
A Product selection and its desired Quantity within a Cart. It does not guarantee a Product's price, active status, or stock.
_Avoid_: Order Item, line item

**Quantity**:
The positive whole number of units selected for a Product.
_Avoid_: Amount, count
