import CategoryOutlinedIcon from '@mui/icons-material/CategoryOutlined'
import DashboardOutlinedIcon from '@mui/icons-material/DashboardOutlined'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import PeopleOutlinedIcon from '@mui/icons-material/PeopleOutlined'
import ShoppingBagOutlinedIcon from '@mui/icons-material/ShoppingBagOutlined'
import StorefrontOutlinedIcon from '@mui/icons-material/StorefrontOutlined'
import { NavLink } from 'react-router-dom'

const navItems = [
  { to: '/', label: 'Dashboard', icon: DashboardOutlinedIcon },
  { to: '/products', label: 'Products', icon: Inventory2OutlinedIcon },
  { to: '/categories', label: 'Categories', icon: CategoryOutlinedIcon },
  { to: '/orders', label: 'Orders', icon: ShoppingBagOutlinedIcon },
  { to: '/customers', label: 'Customers', icon: PeopleOutlinedIcon },
  { to: '/storefront', label: 'Storefront', icon: StorefrontOutlinedIcon },
]

export function AdminSidebar({ onNavigate }: { onNavigate?: () => void }) {
  return (
    <nav aria-label="Sidebar navigation" className="menu menu-lg w-full space-y-2">
      <div className="px-2 pb-3">
        <p className="text-[10px] font-semibold uppercase tracking-[0.28em] text-base-content/60">Navigation</p>
      </div>
      <ul className="space-y-1">
        {navItems.map(({ to, label, icon: Icon }) => (
          <li key={to}>
            <NavLink
              to={to}
              end={to === '/'}
              onClick={onNavigate}
              className={({ isActive }) =>
                ['rounded-xl px-3 py-2 text-sm font-medium transition', isActive ? 'bg-primary text-primary-content' : 'text-base-content hover:bg-base-200'].join(' ')
              }
            >
              <Icon aria-hidden="true" fontSize="small" />
              <span>{label}</span>
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}
