# Domain model

## Account
An Account is the identity used to sign in and access the application. It carries the person's email, credentials, and enabled status. Email is the primary identifier. An Account is granted one or more Roles that determine what it can do. An Account knows nothing about shopping.

## Customer
A Customer is the shopping persona linked to an Account. Only Accounts with the CUSTOMER role have a corresponding Customer row. A Customer owns the shopping-related data: Addresses, Cart, and Orders. The Customer shares its primary key with the Account it extends.

_Avoid_: customer Account, buyer, shopper

## Role
A Role grants permissions to an Account. At minimum: CUSTOMER (storefront access) and ADMIN (admin dashboard access). Role-based granularity within the admin tier is planned for later.

## Address
An Address is a saved postal destination owned by a Customer. A Customer may have multiple Addresses. Each Address contains the recipient name (person or company), phone number, street, postal code, city, and country. Addresses are mutable: customers can edit them at any time. A future B2B scope may add a VAT ID to delivery addresses, but that is not part of the first draft.

## Product
A Product is an item available for purchase. It has a name, description, price, stock quantity, and image. Products carry a SKU as a stable business identifier and have an active/published status. A Product may be assigned to one or more Categories through a join table.

## Category
A Category groups Products. It has a name and a slug. Categories are flat (no nesting) in the first draft. A Category may contain many Products, and a Product may belong to many Categories.

## Cart
A Cart represents a Customer's current, mutable shopping selection. It belongs to a Customer and contains CartItems. There is one active Cart per Customer.

## CartItem
A CartItem is a line in a Cart: a Product reference and a quantity. The price is not persisted in the CartItem because this is not an order snapshot.

## Order
An Order is a placed purchase. It belongs to a Customer and has an order number, a status, a created timestamp, OrderItems, and totals. An Order references two OrderAddress snapshots: one for invoice, one for delivery.

## OrderItem
An OrderItem is a line in an Order. It captures a snapshot of the Product at purchase time: product reference, product name, SKU, unit price, quantity, and line total. An Order does not change when a Product's name or price is later modified.

## OrderAddress
An OrderAddress is an immutable snapshot of a postal destination captured at order time. It contains the same postal fields as a saved Address (name, phone, street, postal code, city, country) but belongs to the Order and is never updated. An Order has two OrderAddress references: one for invoice, one for delivery. This separation ensures historical orders reflect the address as it was when placed, not the Customer's current saved address.

## Order status
An Order progresses through: PLACED → CONFIRMED → SHIPPED → DELIVERED. CANCELLED is an alternative state from any point.

# Storefront

## Storefront
The Storefront is the public shopping experience. It is the part of the business that presents the shop to customers and exposes the site's public pages.

## Homepage
The Homepage is the entry page for the storefront. It represents the shop's public landing page and the canonical entry point for visitors.

## Public
Public describes every page served to unauthenticated visitors — the homepage, login, and 403 pages — all of which render inside the same Layout. "Public" is the resolved layout-name; it does not replace "Storefront" as a domain concept (the Storefront remains the public shopping experience specifically).

## Layout
The Layout is the single reusable page shell (`index :: page(seo, mainContent, pageScripts)`, body attribute `data-page-layout="public"`) that every public page renders inside. The admin login page is deliberately outside this Layout and carries no SEO metadata.

## Canonical URL
A Canonical URL is the single preferred public address for a page. It tells search engines which URL should be treated as the authoritative version of that page.

## SEO metadata
SEO metadata is the set of public page attributes used to describe a page in search and social-sharing contexts, including title, description, robots directives, and Open Graph content.

## Customer journey
A visitor reaches the storefront through the homepage and, if recognized, may also interact with account-bound features. A Customer can manage multiple Addresses (invoice and delivery), add Products to a Cart, and place Orders with immutable address snapshots. Admins operate in a separate dashboard context where they manage Products, Categories, and Orders. The domain distinguishes between the public storefront experience and the protected account experience.
