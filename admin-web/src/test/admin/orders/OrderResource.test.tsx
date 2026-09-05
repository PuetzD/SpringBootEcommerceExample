import {render, screen} from '@testing-library/react'
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
                id: 'ORD-20260905-ORDERADMIN1',
                orderNumber: 'ORD-20260905-ORDERADMIN1',
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

    expect(await screen.findByText('ORD-20260905-ORDERADMIN1')).toBeTruthy()
    expect(screen.getByText(/19\.99/)).toBeTruthy()
    expect(screen.queryByRole('button', {name: /edit|delete|create/i})).toBeNull()
    expect(consoleError).not.toHaveBeenCalled()
    expect(consoleWarn).not.toHaveBeenCalled()

    consoleError.mockRestore()
    consoleWarn.mockRestore()
  })
})
