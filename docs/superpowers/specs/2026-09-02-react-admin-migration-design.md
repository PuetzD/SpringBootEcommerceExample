# React-Admin Catalog Administration Migration

## Status

Proposed

## Context

The administration frontend currently implements Product and Category management with
page-local React state and custom controls. The backend already exposes stable,
revision-aware Catalog administration APIs, including CSRF protection, pagination,
validation errors, stale-revision conflicts, and category in-use protection.

The administration UI should use React-Admin resources rather than maintaining a
parallel CRUD framework in custom pages. The migration must preserve the existing
backend contracts and the security and concurrency behavior they provide.

## Decision

Replace the custom Product and Category administration pages with React-Admin resources.
Add `react-admin` to `admin-web` and provide a Catalog data provider that translates
React-Admin list, show, create, update, and delete operations to the existing
`/api/admin/products` and `/api/admin/categories` endpoints.

The data provider will use the existing API client so credentials and CSRF headers
remain centralized. Product and Category revisions will be carried in update and
delete requests as `If-Match`/revision data. Backend error codes, including stale
revision and category-in-use conflicts, will be converted into React-Admin errors
with safe user-facing notifications; mutations will not be retried automatically.

The Admin application will declare `products` and `categories` resources. Product
views will support server-side search/status filtering, pagination, create/edit,
activation/deactivation, and category assignment. Category views will support
server-side listing, create/rename, options, and guarded deletion. Existing
Dashboard, Orders, Customers, and Storefront routes remain unchanged.

React-Admin's Material UI presentation layer will replace the custom daisyUI
Product/Category tables and forms. Existing shared shell, authentication/CSRF
bootstrap, backend routes, and unrelated frontend pages remain in place unless a
React-Admin integration requires a narrowly scoped adapter change.

## Data Flow and Error Handling

React-Admin calls the Catalog data provider. The provider delegates HTTP requests to
the existing API client, which adds same-origin credentials and the current CSRF
token. Successful responses are normalized to React-Admin records and list metadata.
Validation errors retain field association where React-Admin supports it. 409 stale
and in-use responses become non-retrying notifications and leave the user on the
current resource page so they can reload and review current state.

## Testing

Frontend tests will cover:

- resource registration and supported routes;
- Product and Category list filters and pagination;
- create and edit payload mapping, including revisions;
- activation/deactivation and guarded deletion;
- CSRF headers and backend error-to-notification mapping;
- accessible labels, loading states, and mutation feedback.

The existing backend API and security tests remain the source of truth for endpoint
authorization, validation, CSRF enforcement, and optimistic-concurrency behavior.

## Alternatives Considered

### Keep the custom pages

Rejected because it duplicates CRUD, pagination, mutation feedback, and resource
state management that React-Admin already provides.

### Use React-Admin only for shared controls

Rejected because it retains the custom resource lifecycle and provides little of the
maintenance benefit while adding the dependency.

### Run React-Admin beside the custom pages behind a flag

Rejected for the current scope because it doubles the administration surface and
requires synchronizing two resource implementations without a migration requirement.
