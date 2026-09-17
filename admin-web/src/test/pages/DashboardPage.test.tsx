import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {QueryClient} from '@tanstack/react-query'
import {AdminContext} from 'react-admin'
import {DashboardPage} from '../../pages/DashboardPage'

const period = {
  from: '2026-08-18T12:00:00.000Z',
  to: '2026-09-17T12:00:00.000Z',
}

function renderDashboard(getList: ReturnType<typeof vi.fn>) {
  const queryClient = new QueryClient({defaultOptions: {queries: {retry: false}}})

  return render(
    <AdminContext dataProvider={{getList}} queryClient={queryClient}>
      <DashboardPage />
    </AdminContext>,
  )
}

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.spyOn(Date, 'now').mockReturnValue(Date.parse('2026-09-17T12:00:00Z'))
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows rolling-period metrics and recent orders from the existing resources', async () => {
    const getList = vi.fn(async (resource: string) => {
      if (resource === 'orders') {
        return {
          data: [
            {
              id: 'ORD-2026-100001',
              orderId: '00000000-0000-0000-0000-000000000009',
              orderNumber: 'ORD-2026-100001',
              customerId: 7,
              total: 59.99,
              placedAt: '2026-09-16T09:00:00Z',
              items: [],
              addresses: [],
            },
          ],
          total: 3,
          meta: {revenue: 159.97, currency: 'EUR'},
        }
      }

      return {data: [], total: 2}
    })

    renderDashboard(getList)

    expect(await screen.findByText('EUR 159.97')).toBeTruthy()
    expect(screen.getByText('3')).toBeTruthy()
    expect(screen.getByText('2')).toBeTruthy()
    expect(screen.getByText('ORD-2026-100001')).toBeTruthy()

    await waitFor(() => expect(getList).toHaveBeenCalledTimes(2))

    const orderCall = getList.mock.calls.find(([resource]) => resource === 'orders')
    const customerCall = getList.mock.calls.find(([resource]) => resource === 'customers')

    expect(orderCall?.[1]).toMatchObject({
      pagination: {page: 1, perPage: 5},
      sort: {field: 'placedAt', order: 'DESC'},
      filter: period,
    })
    expect(customerCall?.[1]).toMatchObject({
      pagination: {page: 1, perPage: 1},
      sort: {field: 'id', order: 'DESC'},
      filter: period,
    })
  })

  it('shows the shared loading presentation while either resource is pending', () => {
    const getList = vi.fn(() => new Promise(() => {}))

    renderDashboard(getList)

    expect(screen.getByRole('status')).toBeTruthy()
    expect(screen.getByText('Loading dashboard…')).toBeTruthy()
  })

  it('shows one error alert and retries both resource queries', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => {})
    const getList = vi.fn(async (resource: string) => {
      if (resource === 'orders') {
        throw new Error('Dashboard data is unavailable')
      }

      return {data: [], total: 0}
    })

    renderDashboard(getList)

    expect(await screen.findByRole('alert')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', {name: 'Retry'}))

    await waitFor(() => expect(getList).toHaveBeenCalledTimes(4))
  })

  it('shows zero-valued cards and an explicit empty recent-orders state', async () => {
    const getList = vi.fn(async (resource: string) => {
      if (resource === 'orders') {
        return {data: [], total: 0, meta: {revenue: 0, currency: 'EUR'}}
      }

      return {data: [], total: 0}
    })

    renderDashboard(getList)

    expect(await screen.findByText('EUR 0.00')).toBeTruthy()
    expect(screen.getAllByText('0')).toHaveLength(2)
    expect(screen.getByText('No orders in the last 30 days')).toBeTruthy()
  })
})
