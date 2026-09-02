import type { ComponentType } from 'react'
import { Admin, CustomRoutes, type LayoutProps, Resource } from 'react-admin'
import { Route } from 'react-router-dom'
import { CategoriesPage } from '../pages/CategoriesPage'
import { CustomersPage } from '../pages/CustomersPage'
import { DashboardPage } from '../pages/DashboardPage'
import { OrdersPage } from '../pages/OrdersPage'
import { ProductsPage } from '../pages/ProductsPage'
import { StorefrontPage } from '../pages/StorefrontPage'
import { AdminShell } from '../components/admin/AdminShell'
import { dataProvider } from './dataProvider'

function AdminLayout({ children }: LayoutProps) {
  return <AdminShell>{children}</AdminShell>
}

function withResource(Component: ComponentType) {
  return function ResourcePage() {
    return <Component />
  }
}

const ProductResourcePage = withResource(ProductsPage)
const CategoryResourcePage = withResource(CategoriesPage)

export function AdminApp() {
  return (
    <Admin dashboard={DashboardPage} dataProvider={dataProvider} disableTelemetry layout={AdminLayout}>
      <Resource name="products" list={ProductResourcePage} />
      <Resource name="categories" list={CategoryResourcePage} />
      <CustomRoutes>
        <Route path="orders" element={<OrdersPage />} />
        <Route path="customers" element={<CustomersPage />} />
        <Route path="storefront" element={<StorefrontPage />} />
      </CustomRoutes>
    </Admin>
  )
}
