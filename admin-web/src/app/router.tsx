import type { RouteObject } from 'react-router-dom'
import { createBrowserRouter } from 'react-router-dom'
import { AdminApp } from '../admin/AdminApp'

export const ADMIN_BASENAME = '/admin'

export const adminRoutes: RouteObject[] = [
  {
    path: '*',
    element: <AdminApp />,
  },
]

export const router = createBrowserRouter(adminRoutes, {
  basename: ADMIN_BASENAME,
})
