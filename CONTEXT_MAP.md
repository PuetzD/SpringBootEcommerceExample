# Context Map

This modular monolith separates the customer shopping journey into five bounded contexts. Each context owns its language and model. `storefront` and `security` are adapter packages around those contexts rather than domains; the architecture rules still isolate them as protected slices (so no context internal may be reached from them), exactly as the bounded contexts are protected. See `ArchitectureRulesTest`.

Shared presentation support lives in `shared.web` (e.g. `SeoMetadata`, `CanonicalUrlFactory`). It is owned by no context and is consumed by the web adapters of any public page.

## Contexts

- [Identity and Access](./contexts/account/CONTEXT.md): registers Accounts and establishes who may access the application
- [Customer Profile](./contexts/customer/CONTEXT.md): represents Customers and their saved Addresses
- [Catalog](./contexts/catalog/CONTEXT.md): describes Product families and their sellable Product Variants, Categories, prices, and available stock
- [Cart](./contexts/cart/CONTEXT.md): maintains Product Variant selections for a Customer or anonymous browser session
- [Ordering](./contexts/ordering/CONTEXT.md): checks out a Cart and records the resulting purchase as an Order

## Relationships

An arrow `A → B` means “A depends on B's published contract.” The graph is
acyclic; inbound adapters may depend on published input ports, while context
internals are never shared.

- **Identity and Access → Customer Profile**: When a Customer-role Account is registered, Identity and Access creates the corresponding Customer through Customer Profile's published input port (the dependency arrow points this way). Customer Profile stores the Account identifier as its own `AccountId` value object and does not consume the Account aggregate.
- **Cart → Catalog**: Cart stores Product Variant identifiers and desired quantities. Its delivery adapter asks Catalog for display details; Catalog remains authoritative for family/variant activity, current price, and stock.
- **Ordering → Customer Profile**: Checkout obtains the Customer-owned shipping and billing Address details through Customer Profile's published contract and stores immutable snapshots.
- **Ordering → Cart**: Checkout loads the Customer Cart through Cart's published contract and clears it only when the Order is placed successfully.
- **Ordering → Catalog**: Checkout resolves and purchases each selected Product Variant through Catalog's published contract. Ordering stores the returned product-family and variant identities, SKU, name, price, and quantity as purchase facts.
- **Ordering → Integration platform**: Ordering records versioned immutable integration events in its transactional outbox. Kafka publication is asynchronous and post-commit; events expose identifiers and snapshots, never aggregates or persistence entities.

Customer identifiers in Cart and Ordering and shared `Money` semantics are shared-kernel use, not
consumer/provider relationships. Security and Administration compose published ports as inbound
adapters, so they are not additional bounded contexts or arrows in this graph.

No context shares a persistence entity with another context. Cross-context collaboration uses identifiers, immutable contracts, and application operations.

The portfolio extensibility baseline models German B2C physical-goods commerce in EUR with
SKU-backed Product Variants and account-required checkout. Payment, tax calculation, shipping-rate
calculation, reservation, fulfillment, returns/refunds, and privacy automation remain planned
capabilities rather than current context relationships. The operating assumptions and proposed
boundaries are recorded in [ADR-0007](./docs/adr/0007-commerce-operating-model-baseline.md) and
[ADR-0008](./docs/adr/0008-payment-and-reservation-boundaries.md).

## Identifier value objects

Four identifier value objects are shared across contexts via `sharedkernel.identity` (alongside `Money` in `sharedkernel.money`): `AccountId` (`account`, `customer`), `CustomerId` (`customer`, `cart`, `ordering`), `ProductId` (`catalog`, `cart`, `ordering`), and `ProductVariantId` (`catalog`, `cart`, `ordering`). They are record wrappers over a `long` that enforce the same positivity invariant. `Sku` remains a Catalog-local business identifier; other context-local identifiers stay in their owning context: `CategoryId` (catalog), `AddressId` (customer), `CartId`, `GuestCartId`, `CartOwner`, `Quantity` (cart), `OrderId`, `OrderNumber`, `CheckoutId`, `AddressRole` (ordering).

The decision to promote the shared identifiers into `sharedkernel` is recorded in [ADR-0004](./docs/adr/0004-shared-kernel-identifiers-and-money.md). Product-family and ProductVariant identity and migration rules are recorded in [ADR-0010](./docs/adr/0010-product-variants-and-sellable-identity.md). Administration's delivery-channel boundary is recorded in [ADR-0005](./docs/adr/0005-administration-as-catalog-delivery-channel.md).
