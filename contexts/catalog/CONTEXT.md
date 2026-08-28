# Catalog

Catalog describes the Products the shop may present and sell. For this project it also owns the shop-wide stock quantity; warehouse inventory is outside the model.

## Language

**Product**:
A distinct item the shop may offer for purchase, identified by a stable SKU. It has descriptive details, a current Price, an active status, and a Stock Quantity.
_Avoid_: Item, article, merchandise

**SKU**:
The stable, human-recognizable business identifier assigned to a Product.
_Avoid_: Product code, item number

**Category**:
A flat grouping used to organize Products in the Catalog. A Product may belong to multiple Categories.
_Avoid_: Collection, department, nested category

**Price**:
The current Money amount charged for one unit of a Product when an Order is placed.
_Avoid_: Cart price, quoted price

**Stock Quantity**:
The number of Product units currently available to place in Orders across the whole shop.
_Avoid_: Inventory, warehouse stock

**Active Product**:
A Product eligible to appear in customer-facing Catalog results and to be purchased. Active does not imply that the Product is currently in stock.
_Avoid_: Published Product, available Product

**Money**:
A non-negative monetary amount with two-decimal precision. Amount determines monetary equality because this shop uses one implicit currency.
_Avoid_: Decimal, currency-specific Money
