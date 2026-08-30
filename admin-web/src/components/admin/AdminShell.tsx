import { Outlet, useLocation } from 'react-router-dom'
import { useMemo, useState } from 'react'
import { AdminHeader } from './AdminHeader'
import { AdminSidebar } from './AdminSidebar'
import { Breadcrumbs } from './Breadcrumbs'
import { FeedbackBanner } from './FeedbackBanner'

const navLabels: Record<string, string> = {
  '/': 'Dashboard',
  '/products': 'Products',
  '/categories': 'Categories',
  '/orders': 'Orders',
  '/customers': 'Customers',
  '/storefront': 'Storefront',
}

export function AdminShell() {
  const [isDrawerOpen, setIsDrawerOpen] = useState(false)
  const location = useLocation()

  const currentPage = useMemo(() => {
    return navLabels[location.pathname] ?? 'Dashboard'
  }, [location.pathname])

  return (
    <div className="min-h-screen bg-base-200 text-base-content">
      <a href="#admin-main" className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded focus:bg-primary focus:px-4 focus:py-2 focus:text-primary-content">
        Skip to main content
      </a>

      <div className="drawer lg:drawer-open">
        <input
          id="admin-drawer"
          type="checkbox"
          className="drawer-toggle"
          checked={isDrawerOpen}
          aria-label="Toggle sidebar navigation"
          onChange={(event) => setIsDrawerOpen(event.target.checked)}
        />

        <div className="drawer-content flex min-h-screen flex-col">
          <AdminHeader isDrawerOpen={isDrawerOpen} onToggle={() => setIsDrawerOpen((value) => !value)} />

          <div className="flex-1 px-4 pb-8 pt-4 lg:px-8">
            <Breadcrumbs />
            <FeedbackBanner title={`${currentPage} overview`} message="Everything is running normally." />
            <main id="admin-main" tabIndex={-1} className="mt-4">
              <Outlet />
            </main>
          </div>
        </div>

        <div className="drawer-side z-30">
          <label htmlFor="admin-drawer" className="drawer-overlay" aria-label="Close navigation" onClick={() => setIsDrawerOpen(false)} />
          <aside className="min-h-full w-72 bg-base-100 p-4 shadow-xl">
            <AdminSidebar onNavigate={() => setIsDrawerOpen(false)} />
          </aside>
        </div>
      </div>
    </div>
  )
}
