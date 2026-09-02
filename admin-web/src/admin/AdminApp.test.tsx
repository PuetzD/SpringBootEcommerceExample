import { render, screen } from '@testing-library/react'
import { RouterProvider, createMemoryRouter } from 'react-router-dom'
import { CsrfProvider } from '../auth/CsrfProvider'
import { ADMIN_BASENAME, adminRoutes } from '../app/router'

function renderRoute(initialEntry: string) {
  const router = createMemoryRouter(adminRoutes, {
    basename: ADMIN_BASENAME,
    initialEntries: [initialEntry],
  })

  return render(
    <CsrfProvider>
      <RouterProvider router={router} />
    </CsrfProvider>,
  )
}

describe('AdminApp', () => {
  it('renders registered product and category navigation entries', () => {
    renderRoute('/admin')

    expect(screen.getByRole('link', { name: /products/i }).getAttribute('href')).toBe('/admin/products')
    expect(screen.getByRole('link', { name: /categories/i }).getAttribute('href')).toBe('/admin/categories')
  })

  it.each(['/admin', '/admin/products', '/admin/categories', '/admin/orders', '/admin/customers', '/admin/storefront'])
  ('supports %s without replacing preserved routes', (path) => {
    renderRoute(path)

    expect(screen.getByRole('main')).toBeTruthy()
  })
})
