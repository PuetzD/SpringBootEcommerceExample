import { ComponentType, lazy, Suspense } from 'react'
import { createBrowserRouter, Navigate } from 'react-router-dom'
import { AdminShell } from '../components/admin/AdminShell'
import { LoadingState } from '../components/admin/LoadingState'

const withLoading = (Component: ComponentType) => (
  <Suspense fallback={<LoadingState message="Loading page..." />}>
    <Component />
  </Suspense>
)

const DashboardPage = lazy(() =>
  import('../pages/DashboardPage').then((module) => ({ default: module.DashboardPage })),
)
const ProductsPage = lazy(() =>
  import('../pages/ProductsPage').then((module) => ({ default: module.ProductsPage })),
)
const CategoriesPage = lazy(() =>
  import('../pages/CategoriesPage').then((module) => ({ default: module.CategoriesPage })),
)
const OrdersPage = lazy(() =>
  import('../pages/OrdersPage').then((module) => ({ default: module.OrdersPage })),
)
const CustomersPage = lazy(() =>
  import('../pages/CustomersPage').then((module) => ({ default: module.CustomersPage })),
)
const StorefrontPage = lazy(() =>
  import('../pages/StorefrontPage').then((module) => ({ default: module.StorefrontPage })),
)

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AdminShell />,
    children: [
      { index: true, element: withLoading(DashboardPage) },
      { path: 'products', element: withLoading(ProductsPage) },
      { path: 'categories', element: withLoading(CategoriesPage) },
      { path: 'orders', element: withLoading(OrdersPage) },
      { path: 'customers', element: withLoading(CustomersPage) },
      { path: 'storefront', element: withLoading(StorefrontPage) },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])
