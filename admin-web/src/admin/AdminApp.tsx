import { Admin, CustomRoutes, type LayoutProps, Resource } from 'react-admin'
import { Route } from 'react-router-dom'
import { CustomersPage } from '../pages/CustomersPage'
import { DashboardPage } from '../pages/DashboardPage'
import { StorefrontPage } from '../pages/StorefrontPage'
import { AdminShell } from '../components/admin/AdminShell'
import { dataProvider } from './dataProvider'
import {CategoryCreate} from './categories/CategoryCreate'
import {CategoryEdit} from './categories/CategoryEdit'
import {CategoryList} from './categories/CategoryList'
import {ProductCreate} from './products/ProductCreate'
import {ProductEdit} from './products/ProductEdit'
import {ProductList} from './products/ProductList'
import {OrderList} from './orders/OrderList'
import {OrderShow} from './orders/OrderShow'

function AdminLayout({ children }: LayoutProps) {
  return <AdminShell>{children}</AdminShell>
}

export function AdminApp() {
  return (
    <Admin dashboard={DashboardPage} dataProvider={dataProvider} disableTelemetry layout={AdminLayout}>
      <Resource name="products" list={ProductList} create={ProductCreate} edit={ProductEdit} />
      <Resource name="categories" list={CategoryList} create={CategoryCreate} edit={CategoryEdit} />
      <Resource name="orders" list={OrderList} show={OrderShow} />
      <CustomRoutes>
        <Route path="customers" element={<CustomersPage />} />
        <Route path="storefront" element={<StorefrontPage />} />
      </CustomRoutes>
    </Admin>
  )
}
