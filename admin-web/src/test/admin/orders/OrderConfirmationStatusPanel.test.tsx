import {render, screen, waitFor} from '@testing-library/react'
import {AdminContext, RecordContextProvider} from 'react-admin'
import type {Order, OrderConfirmationStatusRecord} from '../../../api/types'
import {OrderConfirmationStatusPanel} from '../../../admin/orders/OrderConfirmationStatusPanel'

const order: Order = {
  id: 'ORD-2026-100002',
  orderId: '00000000-0000-0000-0000-000000000010',
  orderNumber: 'ORD-2026-100002',
  customerId: 7,
  total: 19.99,
  placedAt: '2026-09-05T09:00:00Z',
  items: [],
  addresses: [],
}

const confirmationStatus: OrderConfirmationStatusRecord = {
  id: order.orderNumber,
  status: 'FAILED',
  failedAttempts: 2,
  lastError: 'SMTP unavailable',
  retryAt: '2026-09-05T09:05:00Z',
  sentAt: null,
}

it('loads and displays confirmation status for the current order', async () => {
  const getOne = vi.fn().mockResolvedValue({data: confirmationStatus})

  render(
    <AdminContext dataProvider={{getOne}}>
      <RecordContextProvider value={order}>
        <OrderConfirmationStatusPanel />
      </RecordContextProvider>
    </AdminContext>,
  )

  await waitFor(() =>
    expect(getOne).toHaveBeenCalledWith('orderConfirmationStatus', {
      id: order.orderNumber,
    }),
  )
  expect(await screen.findByText('FAILED')).toBeTruthy()
  expect(screen.getByText('2')).toBeTruthy()
  expect(screen.getByText('SMTP unavailable')).toBeTruthy()
})
