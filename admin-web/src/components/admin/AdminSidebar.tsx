import { NavLink } from 'react-router-dom'

const navItems = [
  { to: '/', label: 'Dashboard', icon: '¦' },
  { to: '/products', label: 'Products', icon: '?' },
  { to: '/categories', label: 'Categories', icon: '?' },
  { to: '/orders', label: 'Orders', icon: '?' },
  { to: '/customers', label: 'Customers', icon: '?' },
  { to: '/storefront', label: 'Storefront', icon: '?' },
]

export function AdminSidebar({ onNavigate }: { onNavigate?: () => void }) {
  return (
    <nav aria-label="Sidebar navigation" className="menu menu-lg w-full space-y-2">
      <div className="px-2 pb-3">
        <p className="text-[10px] font-semibold uppercase tracking-[0.28em] text-base-content/60">Navigation</p>
      </div>
      <ul className="space-y-1">
        {navItems.map(({ to, label, icon }) => (
          <li key={to}>
            <NavLink
              to={to}
              end={to === '/'}
              onClick={onNavigate}
              className={({ isActive }) =>
                ['rounded-xl px-3 py-2 text-sm font-medium transition', isActive ? 'bg-primary text-primary-content' : 'text-base-content hover:bg-base-200'].join(' ')
              }
            >
              <span aria-hidden="true" className="text-base">{icon}</span>
              <span>{label}</span>
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}
