import {useState, type ReactNode} from 'react'
import {Outlet} from 'react-router-dom'
import {AdminHeader} from './AdminHeader'
import {AdminSidebar} from './AdminSidebar'
import {Breadcrumbs} from './Breadcrumbs'


type AdminShellProps = {
    children?: ReactNode
}

export function AdminShell({children}: AdminShellProps) {
    const [isDrawerOpen, setIsDrawerOpen] = useState(false)

    return (
        <div className="min-h-screen bg-base-200 text-base-content">
            <a href="#admin-main"
               className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded focus:bg-primary focus:px-4 focus:py-2 focus:text-primary-content">
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
                    <AdminHeader isDrawerOpen={isDrawerOpen} onToggle={() => setIsDrawerOpen((value) => !value)}/>

                    <div className="flex-1 px-4 pb-8 pt-4 lg:px-8">
                        <Breadcrumbs/>
                        <main id="admin-main" tabIndex={-1} className="mt-4">
                            {children ?? <Outlet/>}
                        </main>
                    </div>
                </div>

                <div className="drawer-side z-30">
                    <label htmlFor="admin-drawer" className="drawer-overlay" aria-label="Close navigation"
                           onClick={() => setIsDrawerOpen(false)}/>
                    <aside className="min-h-full w-72 bg-base-100 p-4 shadow-xl">
                        <AdminSidebar onNavigate={() => setIsDrawerOpen(false)}/>
                    </aside>
                </div>
            </div>
        </div>
    )
}
