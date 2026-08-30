import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AdminShell } from './AdminShell'
import { ConfirmDialog } from './ConfirmDialog'
import { EmptyState } from './EmptyState'
import { LoadingState } from './LoadingState'

describe('AdminShell', () => {
  it('renders navigation links with accessible names and routes', () => {
    render(
      <MemoryRouter initialEntries={['/products']}>
        <AdminShell>
          <div>Products page</div>
        </AdminShell>
      </MemoryRouter>,
    )

    const dashboardLink = screen.getByRole('link', { name: /dashboard/i })
    const productsLink = screen.getByRole('link', { name: /products/i })

    expect(dashboardLink.getAttribute('href')).toBe('/')
    expect(productsLink.getAttribute('href')).toBe('/products')
    expect(productsLink.getAttribute('aria-current')).toBe('page')
  })

  it('allows the mobile drawer to open and close with a native button', () => {
    render(
      <MemoryRouter initialEntries={['/customers']}>
        <AdminShell>
          <div>Customers page</div>
        </AdminShell>
      </MemoryRouter>,
    )

    const toggleButton = screen.getByRole('button', { name: /open navigation/i })
    const drawer = screen.getByRole('navigation', { name: /sidebar navigation/i })

    expect(toggleButton.getAttribute('aria-expanded')).toBe('false')
    fireEvent.click(toggleButton)
    expect(toggleButton.getAttribute('aria-expanded')).toBe('true')
    expect(drawer).toBeTruthy()

    fireEvent.click(toggleButton)
    expect(toggleButton.getAttribute('aria-expanded')).toBe('false')
  })

  it('shows meaningful loading and empty state text', () => {
    render(<LoadingState message="Loading products" />)
    render(<EmptyState title="No products" description="Add your first product to get started." />)

    expect(screen.getByText(/loading products/i)).toBeTruthy()
    expect(screen.getByText(/no products/i)).toBeTruthy()
    expect(screen.getByText(/add your first product to get started/i)).toBeTruthy()
  })

  it('requires an explicit confirmation action', () => {
    const onConfirm = vi.fn()
    const onCancel = vi.fn()

    render(
      <ConfirmDialog
        open={true}
        title="Delete product"
        message="This action cannot be undone."
        confirmLabel="Delete product"
        cancelLabel="Cancel"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />,
    )

    fireEvent.click(screen.getByRole('button', { name: /delete product/i }))
    fireEvent.click(screen.getByRole('button', { name: /cancel/i }))

    expect(onConfirm).toHaveBeenCalledTimes(1)
    expect(onCancel).toHaveBeenCalledTimes(1)
  })
})
