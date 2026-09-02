import { render, screen } from '@testing-library/react'
import { RouterProvider, createMemoryRouter } from 'react-router-dom'
import { CsrfProvider } from '../auth/CsrfProvider'
import { ADMIN_BASENAME, adminRoutes } from '../app/router'

vi.mock('../pages/ProductsPage', () => ({
  ProductsPage: () => <h2>Products resource</h2>,
}))

vi.mock('../pages/CategoriesPage', () => ({
  CategoriesPage: () => <h2>Categories resource</h2>,
}))

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

  it.each([
    ['/admin', /^dashboard$/i],
    ['/admin/products', /products resource/i],
    ['/admin/categories', /categories resource/i],
    ['/admin/orders', /^orders$/i],
    ['/admin/customers', /^customers$/i],
    ['/admin/storefront', /^storefront$/i],
  ])('supports %s without replacing preserved non-resource routes', (path, heading) => {
    renderRoute(path)

    expect(screen.getByRole('heading', { name: heading })).toBeTruthy()
  })
})
