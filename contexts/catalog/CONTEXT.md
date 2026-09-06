# Catalog

Catalog describes the Products the shop may present and sell. For this project it also owns the shop-wide stock quantity; warehouse inventory is outside the model.

## Language

**Product**:
A customer-facing product family that groups one or more Product Variants sharing descriptive details and Category membership. A Product is not itself the sellable SKU once variants are introduced.
_Avoid_: Item, article, merchandise

**Product Variant**:
A distinct sellable form of a Product, identified by a stable SKU. It owns the current Price, Stock Quantity, active status, and variant-specific facts. An existing simple Product is migrated to one default Product Variant.
_Avoid_: Option, configuration, child product

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

**Inactive Product**:
A Product retained in the Catalog but not eligible to appear in customer-facing results or be purchased. Deactivation preserves its identity, SKU, history, and category membership.
_Avoid_: Deleted Product, unavailable Product

**Category membership**:
The relationship between a Product and a Category. A Product may have multiple memberships, and membership counts include both active and inactive Products.
_Avoid_: Product ownership, category inventory

**Product attribute**:
A structured, named fact describing a Product or Product Variant. Product-level attributes apply to every variant; variant-level attributes distinguish sellable forms and may be used for selection, filtering, or merchandising rules.
_Avoid_: Free-form metadata, tag, badge

**Badge**:
A customer-facing merchandising label derived from catalog facts or explicitly defined merchandising rules. A Badge is presentation output, not a Product attribute or a replacement for Product status.
_Avoid_: Product status, tag, arbitrary label

**Money**:
A non-negative EUR monetary amount with two-decimal precision. New commercial records carry explicit EUR currency metadata, and amounts from another currency cannot be combined.
_Avoid_: Decimal, implicit currency, exchange rate
