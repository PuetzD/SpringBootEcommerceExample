# Read-only Customer Administration Design

## Status

Approved for implementation planning.

## Goal

Replace the placeholder admin Customers page with a read-only customer
workflow:

    Customers list -> search by email or name -> Customer detail

The detail view includes saved addresses and links to the customer's order
details. Customer, account, and order mutations are out of scope.

## Domain ownership

Customer Profile owns the Customer identity, including `givenName`,
`familyName`, `contactEmail`, and saved Addresses. Identity and Access owns
the Account login email and authentication data. Ordering owns Order history
and Order Number identity.

`contactEmail` and Account login email are deliberately separate concepts. A
new registration initializes both from the submitted email, but a future
contact-email change does not have to change the sign-in identifier. The
Account aggregate remains authentication-only and is not needed by the
Customer administration query.

The original `V1__create_account_schema.sql` is updated directly because the
local database is disposable and will be dropped and recreated. Customer
registration requires both names. The form labels are “First name” and “Last
name”; domain and API fields are `givenName` and `familyName`.

## Architectural boundary

Administration owns the protected HTTP and React Admin delivery adapters.
Customer owns customer-profile query behavior and contact-email search;
Ordering owns order-history query behavior. No adapter accesses another
context's domain object, repository, persistence entity, or service directly.

The Customer administration persistence adapter queries only Customer-owned
data. It maps directly to application view records and keeps pagination and
sorting inside the Customer adapter.

Order summaries are obtained through Ordering's published administration query
port and composed only for Customer detail. Each order links by its business
Order Number to the existing admin Order detail route.

Dependency direction:

    React Admin
        -> /api/admin/customers
        -> Administration REST adapter
        -> Customer administration query port
        -> Customer read-only query adapter
        -> Customer read data

    Customer detail composition
        -> Ordering administration query port
        -> Order summaries by Customer ID

## Application contracts

Add Customer application input contracts for:

- `CustomerAdministrationQuery`
- `CustomerAdminSearch(page, size, query)`
- `CustomerAdminPage`
- `CustomerAdminSummary`
- `CustomerAdminDetail`
- immutable administrative address and order-summary views

The list summary contains Customer ID, given name, family name, and contact
email.
Search is case-insensitive and matches partial text against email, given name,
or family name. Results are deterministic and newest Customer ID first unless
the existing Customer persistence model gains an explicit creation timestamp.

The detail contains Customer ID, names, contact email, Account ID, saved address
snapshots, and order summaries containing Order Number, internal order UUID,
total, and placed-at timestamp. It does not contain credentials, password
data, payment state, shipment state, or mutation controls.

## REST API

Add the protected read-only endpoints:

- `GET /api/admin/customers?page=0&size=20&q=alice`
- `GET /api/admin/customers/{customerId}`

The list uses the existing `PageResponse` envelope. The response `id` is the
Customer ID because it is the stable React Admin resource identifier. The
detail response includes order links using Order Number and the existing
`/admin/orders/{orderNumber}` route.

Invalid page or size returns `400`; an unknown Customer ID returns `404`; an
empty search returns an empty page; and no POST, PUT, or DELETE endpoints are
added.

## React Admin

Register Customers as a read-only `Resource` with list and show components.
The list has an always-on search input labelled “Email or name”, Customer ID,
name, and email columns. The detail displays identity, addresses, and order
summaries. Order Numbers are links to the Order resource detail route.

Use the existing React Admin components, admin shell, and daisyUI conventions.
Do not add create, edit, delete, account disable, address mutation, or order
mutation controls.

## Verification

Add tests at each boundary:

- Customer domain/application tests for required names and immutable admin
  views;
- registration controller/service tests for first-name and last-name input;
- persistence integration tests for name/email search, pagination, ordering,
  and address projection;
- controller tests for list/detail success, invalid pagination, empty search,
  unknown Customer ID, and protected access;
- React Admin data-provider tests for page conversion, query forwarding, and
  Customer detail loading;
- React resource tests for read-only columns, addresses, linked orders, and
  absence of mutation controls.

Run the backend and frontend focused suites first, then the full backend test
suite and admin production build. Persistence integration tests require the
project's Docker/Testcontainers environment.

## Documentation impact

Update the Customer context glossary with Customer names and the
administrative Customer View. Update the context map only if the new
administration composition changes the documented relationships. Do not edit
existing ADRs unless the cross-context read projection becomes a durable,
hard-to-reverse ownership decision.
