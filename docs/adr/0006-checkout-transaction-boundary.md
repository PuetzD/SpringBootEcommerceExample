# ADR 0006: Checkout transaction boundary

Status: Accepted

`CheckoutService.place` owns one PostgreSQL transaction for the synchronous
checkout consistency boundary. Order persistence, catalog stock deduction,
integration-outbox append, and customer-cart clearing either all commit or all
roll back. Catalog purchase and customer-cart clearing use mandatory transaction
propagation because they are application operations called within checkout.

The outbox is published asynchronously after checkout commits. If the system is
later split into services, this boundary must be replaced by a reservation and
process-manager/saga protocol; that architecture is intentionally out of scope
for the modular monolith.
