# Context Map

This modular monolith separates the customer shopping journey into five bounded contexts. Each context owns its language and model. `storefront` and `security` are adapter packages around those contexts rather than domains; the architecture rules still isolate them as protected slices (so no context internal may be reached from them), exactly as the bounded contexts are protected. See `ArchitectureRulesTest`.

Shared presentation support lives in `shared.web` (e.g. `SeoMetadata`, `CanonicalUrlFactory`). It is owned by no context and is consumed by the web adapters of any public page.

## Contexts

- [Identity and Access](./contexts/account/CONTEXT.md): registers Accounts and establishes who may access the application
- [Customer Profile](./contexts/customer/CONTEXT.md): represents Customers and their saved Addresses
- [Catalog](./contexts/catalog/CONTEXT.md): describes Products offered for sale and their Categories, prices, and available stock
- [Cart](./contexts/cart/CONTEXT.md): maintains the current shopping selection for a Customer or anonymous browser session
- [Ordering](./contexts/ordering/CONTEXT.md): checks out a Cart and records the resulting purchase as an Order

## Relationships

- **Identity and Access → Customer Profile**: When a Customer-role Account is registered, Identity and Access creates the corresponding Customer through Customer Profile's published input port (the dependency arrow points this way). Customer Profile stores the Account identifier as its own `AccountId` value object and does not consume the Account aggregate.
- **Customer Profile → Cart**: A Customer Cart identifies its owner by Customer identifier and does not consume the Customer aggregate. A Guest Cart instead uses an opaque identifier associated with the anonymous browser session.
- **Identity and Access → Cart**: After successful sign-in, Cart merges the Guest Cart into the Customer Cart by adding quantities for matching Products, persists the Customer Cart, and then removes the Guest Cart.
- **Catalog → Cart**: Cart identifies selections by Product identifier. Catalog remains authoritative for whether a Product is active and for its current price and stock.
- **Customer Profile + Catalog + Cart → Ordering**: Ordering obtains customer-owned Address details, the current Cart selection, and current purchasable Product details through context contracts. It stores snapshots rather than foreign aggregates.
- **Catalog ↔ Ordering**: Catalog and Ordering share the stable meaning of Money. Ordering snapshots the current Catalog price when an Order is placed.
- **Ordering → Catalog + Cart**: Successful checkout decreases Catalog stock and clears the Cart atomically with Order creation.

No context shares a persistence entity with another context. Cross-context collaboration uses identifiers, immutable contracts, and application operations.

## Identifier value objects

Three identifier value objects are shared across contexts via `sharedkernel.identity` (alongside `Money` in `sharedkernel.money`): `AccountId` (`account`, `customer`), `CustomerId` (`customer`, `cart`, `ordering`), and `ProductId` (`catalog`, `cart`, `ordering`). They are byte-identical `record` wrappers over a `long` that all enforce the same positivity invariant — the duplication is accidental, not a modeling distinction. Each context attaches no different meaning to its copy. Context-local identifiers are not shared and stay in their owning context: `Sku`, `CategoryId` (catalog), `AddressId` (customer), `CartId`, `GuestCartId`, `CartOwner`, `Quantity` (cart), `OrderId`, `OrderNumber`, `CheckoutId`, `AddressRole` (ordering).

The decision to promote the shared identifiers into `sharedkernel` is **accepted** — see ADR-0001.
