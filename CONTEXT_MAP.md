# Context Map

This modular monolith separates the customer shopping journey into five bounded contexts. Each context owns its language and model; storefront and security code are adapters around those contexts rather than contexts themselves.

Shared presentation support lives in `shared.web` (e.g. `SeoMetadata`, `CanonicalUrlFactory`). It is owned by no context and is consumed by the web adapters of any public page.

## Contexts

- [Identity and Access](./contexts/account/CONTEXT.md): registers Accounts and establishes who may access the application
- [Customer Profile](./contexts/customer/CONTEXT.md): represents Customers and their saved Addresses
- [Catalog](./contexts/catalog/CONTEXT.md): describes Products offered for sale and their Categories, prices, and available stock
- [Cart](./contexts/cart/CONTEXT.md): maintains the current shopping selection for a Customer or anonymous browser session
- [Ordering](./contexts/ordering/CONTEXT.md): checks out a Cart and records the resulting purchase as an Order

## Relationships

- **Identity and Access → Customer Profile**: Customer Profile receives an Account identifier when a Customer-role Account is registered. It does not consume the Account aggregate.
- **Customer Profile → Cart**: A Customer Cart identifies its owner by Customer identifier and does not consume the Customer aggregate. A Guest Cart instead uses an opaque identifier associated with the anonymous browser session.
- **Identity and Access → Cart**: After successful sign-in, Cart merges the Guest Cart into the Customer Cart by adding quantities for matching Products, persists the Customer Cart, and then removes the Guest Cart.
- **Catalog → Cart**: Cart identifies selections by Product identifier. Catalog remains authoritative for whether a Product is active and for its current price and stock.
- **Customer Profile + Catalog + Cart → Ordering**: Ordering obtains customer-owned Address details, the current Cart selection, and current purchasable Product details through context contracts. It stores snapshots rather than foreign aggregates.
- **Catalog ↔ Ordering**: Catalog and Ordering share the stable meaning of Money. Ordering snapshots the current Catalog price when an Order is placed.
- **Ordering → Catalog + Cart**: Successful checkout decreases Catalog stock and clears the Cart atomically with Order creation.

No context shares a persistence entity with another context. Cross-context collaboration uses identifiers, immutable contracts, and application operations.
