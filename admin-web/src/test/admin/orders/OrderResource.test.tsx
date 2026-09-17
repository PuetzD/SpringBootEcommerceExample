import {render, screen, within} from '@testing-library/react'
import {AdminContext, ResourceContextProvider} from 'react-admin'
import {OrderList} from '../../../admin/orders/OrderList'

describe('Order resource', () => {
  it('renders read-only order columns and no mutation actions', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    const consoleWarn = vi.spyOn(console, 'warn').mockImplementation(() => {})

    render(
      <AdminContext
        dataProvider={{
          getList: vi.fn().mockResolvedValue({
            data: [
              {
                id: 'ORD-2026-100001',
                orderId: '00000000-0000-0000-0000-000000000009',
                orderNumber: 'ORD-2026-100001',
                customerId: 7,
                total: 19.99,
                placedAt: '2026-09-05T09:00:00Z',
                items: [],
                addresses: [],
              },
            ],
            total: 1,
          }),
        }}
      >
        <ResourceContextProvider value="orders">
          <OrderList />
        </ResourceContextProvider>
      </AdminContext>,
    )

    expect(await screen.findByText('ORD-2026-100001')).toBeTruthy()
    expect(within(screen.getByRole('table')).queryByRole('button', {name: 'ra.sort.sort_by'})).toBeNull()
    expect(screen.getByText(/19\.99/)).toBeTruthy()
    expect(screen.queryByRole('button', {name: /edit|delete|create/i})).toBeNull()
    expect(consoleError).not.toHaveBeenCalled()
    expect(consoleWarn).not.toHaveBeenCalled()

    consoleError.mockRestore()
    consoleWarn.mockRestore()
  })
})
