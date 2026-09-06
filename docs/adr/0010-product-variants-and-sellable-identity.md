# ADR 0010: Product Variant Identity and Compatibility Migration

## Status

Accepted on 2026-09-06. Supersedes the variant-deferred portion of ADR 0007.

## Context

The Catalog currently models each Product as one sellable SKU with price, stock, image, and active state. Cart and Ordering identify selections through ProductId. This shape prevents multiple sellable forms of one customer-facing product and would make SKU changes unsafe if SKU became the cross-context identity.

## Decision

`Product` becomes the customer-facing product family. `ProductVariant` becomes the sellable form and owns its stable `ProductVariantId`, Catalog-local `Sku`, price, stock quantity, image, active state, and variant-specific facts. `ProductVariantId` is a positive shared-kernel identifier used by Catalog, Cart, and Ordering. SKU is a human-recognizable Catalog business identifier and is not used as a cross-context foreign identity.

`ProductId` remains the Product-family identity for existing URLs and compatibility reads. The legacy Product SKU remains a temporary alias to the default Variant SKU. A Product always has at least one Variant and exactly one default Variant; the last Variant cannot be deleted. Creating a Product and its default Variant is one atomic Catalog operation.

During the additive migration, the existing Product SKU, price, stock, image, and active columns remain as deprecated frozen storage. They are copied into the default Variant during backfill, then cease to be application write sources. All application reads resolve the default or selected Variant, and all new writes target a Variant. Compatibility operations that cannot identify a Variant explicitly resolve the default Variant while the alias is valid; ambiguous legacy sellable-field writes return a conflict once a Product has multiple Variants. Legacy columns are removed only after compatibility consumers are retired in a later contraction migration.

Variant option combinations are derived from controlled Catalog attribute assignments introduced by a later package. The variant foundation does not add an unbounded attribute map.

## Migration and rollback

V6 is additive and backfills one default Variant for every existing Product. It enforces foreign keys, non-negative price and stock, one default Variant per Product, and unique Variant SKUs. Flyway history prevents normal re-execution; failed migration repair must be followed by a retry and verification. The migration does not provide an automatic down script. Rolling back application code remains compatible because legacy Product columns are retained and the schema additions are additive; destructive column removal requires a separate approved contraction migration after the compatibility window.

## Consequences

Cart and Ordering gain a stable sellable identity independent of SKU edits and can snapshot both Product and Variant references plus SKU, name, price, quantity, and relevant presentation facts. Product-family URLs remain stable. The system temporarily stores frozen duplicate sellable fields, and compatibility behavior must be retired before those columns can be removed.
