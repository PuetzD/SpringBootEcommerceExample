# Catalog

Catalog describes the Products the shop may present and sell. For this project it also owns the shop-wide stock quantity; warehouse inventory is outside the model.

## Language

**Product**:
A customer-facing product family that groups one or more Product Variants sharing descriptive details and Category membership. The Product is the family identity; its Product Variants are the sellable forms.
_Avoid_: Item, article, merchandise

**Product Variant**:
A distinct sellable form of a Product, distinguished by a SKU. It owns the current Price, Stock Quantity, active status, and variant-specific facts.
_Avoid_: Option, configuration, child product

**SKU**:
The stable, human-recognizable business identifier assigned to a Product Variant. A Product family may be addressed through its Product identifier, but SKU identifies the sellable form.
_Avoid_: Product code, item number

**Product Variant Identifier**:
The stable identity of a sellable Product Variant across Catalog, Cart, and Ordering. It is distinct from the human-recognizable SKU, which may be changed only under Catalog policy.
_Avoid_: SKU as foreign identity, option combination

**Category**:
A flat grouping used to organize Products in the Catalog. A Product may belong to multiple Categories.
_Avoid_: Collection, department, nested category

**Price**:
The current Money amount charged for one unit of a Product Variant when an Order is placed.
_Avoid_: Cart price, quoted price

**Stock Quantity**:
The number of units of a Product Variant currently available to place in Orders across the whole shop.
_Avoid_: Inventory, warehouse stock

**Active Product**:
An active Product is eligible to appear in customer-facing Catalog results. Its active Product Variants may be purchased; Product activity alone does not make a Variant active or in stock.
_Avoid_: Published Product, available Product

**Inactive Product**:
A Product retained in the Catalog but not eligible to appear in customer-facing results or have any of its Product Variants purchased. Deactivation preserves its identity, history, and Category membership.
_Avoid_: Deleted Product, unavailable Product

**Category Membership**:
The relationship between a Product and a Category. A Product may have multiple memberships, and membership counts include both active and inactive Products.
_Avoid_: Product ownership, category inventory

**Product Attribute**:
A structured, named fact describing a Product or Product Variant. Product-level attributes apply to every variant; variant-level attributes distinguish sellable forms and may be used for selection, filtering, or merchandising rules.
_Avoid_: Free-form metadata, tag, badge

**Badge**:
A customer-facing merchandising label derived from catalog facts or explicitly defined merchandising rules. A Badge is presentation output, not a Product attribute or a replacement for Product status.
_Avoid_: Product status, tag, arbitrary label

**Money**:
A non-negative monetary amount with explicit currency and two-decimal precision. The current shop's commercial Money is EUR, and amounts in different currencies cannot be combined.
_Avoid_: Decimal, implicit currency, exchange rate
