import {render, screen} from '@testing-library/react'
import {AdminContext, ResourceContextProvider} from 'react-admin'
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
      orderUrl: '/admin/orders/ORD-20260905-ORDERADMIN1',
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
    expect(screen.queryByRole('button', {name: /create|edit|delete/i})).toBeNull()
  })

  it('renders addresses and links order numbers to order detail', async () => {
    render(
      <AdminContext dataProvider={{getOne: vi.fn().mockResolvedValue({data: customer})}}>
        <ResourceContextProvider value="customers">
          <CustomerShow id={customer.id} />
        </ResourceContextProvider>
      </AdminContext>,
    )

    expect(await screen.findByText('alice@example.com')).toBeTruthy()
    expect(screen.getByText('Main Street 1')).toBeTruthy()
    const orderLink = screen.getByRole('link', {name: customer.orders[0].orderNumber})
    expect(orderLink.getAttribute('href')).toBe(`#${customer.orders[0].orderUrl}`)
  })
})
