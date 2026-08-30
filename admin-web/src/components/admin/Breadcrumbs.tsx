import { Link, useLocation } from 'react-router-dom'

const labels: Record<string, string> = {
  '/': 'Dashboard',
  '/products': 'Products',
  '/categories': 'Categories',
  '/orders': 'Orders',
  '/customers': 'Customers',
  '/storefront': 'Storefront',
}

export function Breadcrumbs() {
  const location = useLocation()
  const segments = location.pathname.split('/').filter(Boolean)
  const crumbPath: string[] = []

  const crumbs = segments.map((segment) => {
    crumbPath.push(segment)
    const path = `/${crumbPath.join('/')}`
    return { label: labels[path] ?? segment, path }
  })

  const items = [{ label: 'Dashboard', path: '/' }, ...crumbs]

  return (
    <nav aria-label="Breadcrumb" className="breadcrumbs mb-4 text-sm text-base-content/70">
      <ul>
        {items.map((crumb, index) => {
          const isCurrent = index === items.length - 1
          const isRoot = crumb.path === '/'

          return (
            <li key={`${crumb.path}-${index}`}>
              {isCurrent ? (
                <span aria-current="page">{crumb.label}</span>
              ) : isRoot ? (
                <span>{crumb.label}</span>
              ) : (
                <Link to={crumb.path}>{crumb.label}</Link>
              )}
            </li>
          )
        })}
      </ul>
    </nav>
  )
}
