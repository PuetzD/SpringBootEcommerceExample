import { render, screen, waitFor } from '@testing-library/react'
import { RouterProvider, createMemoryRouter } from 'react-router-dom'
import { CsrfProvider } from '../../auth/CsrfProvider'
import { ADMIN_BASENAME, adminRoutes } from '../../app/router'

const emptyPage = JSON.stringify({content: [], page: 0, size: 20, totalElements: 0, totalPages: 0})

function mockAdminApi() {
  vi.stubGlobal(
    'fetch',
    vi.fn(async (input: RequestInfo | URL) => {
      const url = String(input)
      return new Response(url.includes('/api/admin/') ? emptyPage : '{}', {
        status: 200,
        headers: {'Content-Type': 'application/json'},
      })
    }),
  )
}

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
  beforeEach(() => {
    mockAdminApi()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('renders registered product and category navigation entries', async () => {
    renderRoute('/admin')

    expect(screen.getByRole('link', { name: /products/i }).getAttribute('href')).toBe('/admin/products')
    expect(screen.getByRole('link', { name: /categories/i }).getAttribute('href')).toBe('/admin/categories')
    expect(screen.getByRole('link', { name: /orders/i }).getAttribute('href')).toBe('/admin/orders')
  })

  it.each(['/admin', '/admin/products', '/admin/categories', '/admin/orders', '/admin/customers', '/admin/storefront'])
  ('supports %s without replacing preserved routes or emitting warnings', async (path) => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    const consoleWarn = vi.spyOn(console, 'warn').mockImplementation(() => {})

    renderRoute(path)

    await waitFor(() => expect(screen.getByRole('main')).toBeTruthy())
    expect(consoleError).not.toHaveBeenCalled()
    expect(consoleWarn).not.toHaveBeenCalled()

    consoleError.mockRestore()
    consoleWarn.mockRestore()
  })
})
