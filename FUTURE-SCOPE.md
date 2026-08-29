# Future scope

Concepts explicitly excluded from the first draft. They may be added later but implementing them now would dilute the base project.

## Commerce extensions
- Payment / PaymentMethod (payment processing, refunds)
- Shipment / delivery tracking (separate from Order)
- Shipping method selection and rates (checkout places no shipping charge)
- Tax calculation / VAT (checkout currently places no tax)
- Discount / Coupon / Promotion
- Wishlist
- Review / Rating
- Return / Refund
- Invoice (accounting document, distinct from OrderAddress invoice snapshot)

## Inventory & supply
- Warehouse / Inventory (stock is part of Product for now)
- Supplier
- Multiple warehouses
- Product variants (size, color, etc.)

## B2B
- CustomerCompany
- VAT ID on delivery addresses
- TaxRule

## Identity & access
- Role-based granularity within admin tier (e.g. STAFF, MANAGER)
- AuditLog
- Notification (email, push, etc.)

## Search & architecture
- Full-text search / Elasticsearch
- Event sourcing (persistence pattern — too heavy for this scope)
- Nested categories (categories are flat in v1)
