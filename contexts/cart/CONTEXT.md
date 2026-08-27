# Cart

Cart maintains a Customer's current, mutable shopping selection before checkout. It records intent, not a price or stock commitment.

## Language

**Cart**:
The single active shopping selection owned by a Customer. A Cart contains at most one Cart Item for each selected Product.
_Avoid_: Basket, bag, order

**Cart Item**:
A Product selection and its desired Quantity within a Cart. It does not guarantee a Product's price, active status, or stock.
_Avoid_: Order Item, line item

**Quantity**:
The positive whole number of units selected for a Product.
_Avoid_: Amount, count
