import {fireEvent, render, screen, waitFor, within} from '@testing-library/react'
import {AdminContext, ResourceContextProvider} from 'react-admin'
import {RouterProvider, createMemoryRouter} from 'react-router-dom'
import {CustomerList} from '../../../admin/customers/CustomerList'
import {CustomerShow} from '../../../admin/customers/CustomerShow'

const customer = {
  id: 12,
  givenName: 'Alice',
  familyName: 'Example',
  contactEmail: 'alice@example.com',
  accountId: 44,
  addresses: [
    {
      id: 3,
      recipientName: 'Alice Example',
      companyName: null,
      addressLine1: 'Main Street 1',
      addressLine2: null,
      city: 'Berlin',
      region: null,
      postalCode: '10115',
      countryCode: 'DE',
      phoneNumber: null,
      defaultShipping: true,
      defaultBilling: true,
    },
  ],
  orders: [
    {
      orderNumber: 'ORD-20260905-ORDERADMIN1',
      orderId: '00000000-0000-0000-0000-000000000009',
      total: 19.99,
      placedAt: '2026-09-05T09:00:00Z',
    },
  ],
}

describe('Customer resource', () => {
  it('renders searchable read-only customer columns', async () => {
    render(
      <AdminContext
        dataProvider={{getList: vi.fn().mockResolvedValue({data: [customer], total: 1})}}
      >
        <ResourceContextProvider value="customers">
          <CustomerList />
        </ResourceContextProvider>
      </AdminContext>,
    )

    expect(screen.getByRole('textbox', {name: /email or name/i})).toBeTruthy()
    expect(await screen.findByText('Alice')).toBeTruthy()
    expect(screen.getByText('Example')).toBeTruthy()
    expect(screen.getByText('alice@example.com')).toBeTruthy()
    expect(within(screen.getByRole('table')).queryByRole('button', {name: 'ra.sort.sort_by'})).toBeNull()
    expect(screen.queryByRole('button', {name: /create|edit|delete/i})).toBeNull()
  })

  it('navigates from a customer order to its show route under the admin basename', async () => {
    const router = createMemoryRouter(
      [{
        path: '*',
        element: (
          <AdminContext dataProvider={{getOne: vi.fn().mockResolvedValue({data: customer})}}>
            <ResourceContextProvider value="customers">
              <CustomerShow id={customer.id} />
            </ResourceContextProvider>
          </AdminContext>
        ),
      }],
      {basename: '/admin', initialEntries: [`/admin/customers/${customer.id}/show`]},
    )

    render(<RouterProvider router={router} />)

    expect(await screen.findByText('alice@example.com')).toBeTruthy()
    expect(screen.getByText('Main Street 1')).toBeTruthy()
    const orderLink = screen.getByRole('link', {name: customer.orders[0].orderNumber})
    const orderShowPath = `/admin/orders/${customer.orders[0].orderNumber}/show`
    expect(orderLink.getAttribute('href')).toBe(orderShowPath)

    fireEvent.click(orderLink)
    await waitFor(() => expect(router.state.location.pathname).toBe(orderShowPath))
  })
})
