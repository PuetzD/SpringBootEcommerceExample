import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AdminShell } from '../../../components/admin/AdminShell'

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
    expect(screen.queryByText('?')).toBeNull()
    const sidebar = screen.getByRole('navigation', {name: /sidebar navigation/i})
    expect([...sidebar.querySelectorAll('a')].every((link) => link.querySelector('svg'))).toBe(true)
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

})
