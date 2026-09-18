import {useState} from 'react'
import {Link, useCreatePath, useGetList} from 'react-admin'
import type {Customer, Order, OrderListMeta} from '../api/types'
import {EmptyState} from '../components/admin/EmptyState'
import {LoadingState} from '../components/admin/LoadingState'

const THIRTY_DAYS_MS = 30 * 24 * 60 * 60 * 1000

function lastThirtyDays(now = Date.now()) {
  return {
    from: new Date(now - THIRTY_DAYS_MS).toISOString(),
    to: new Date(now).toISOString(),
  }
}

export function DashboardPage() {
  const [period] = useState(lastThirtyDays)
  const createPath = useCreatePath()
  const orders = useGetList<Order>('orders', {
    pagination: {page: 1, perPage: 5},
    sort: {field: 'placedAt', order: 'DESC'},
    filter: period,
  })
  const customers = useGetList<Customer>('customers', {
    pagination: {page: 1, perPage: 1},
    sort: {field: 'id', order: 'DESC'},
    filter: period,
  })

  if (orders.error || customers.error) {
    return (
      <section className="space-y-4">
        <h2 className="text-2xl font-bold">Dashboard</h2>
        <div role="alert" className="alert alert-error">
          <span>Dashboard data could not be loaded.</span>
          <button
            type="button"
            className="btn btn-sm"
            onClick={() => {
              void orders.refetch()
              void customers.refetch()
            }}
          >
            Retry
          </button>
        </div>
      </section>
    )
  }

  if (orders.isPending || customers.isPending) {
    return (
      <section className="space-y-4">
        <h2 className="text-2xl font-bold">Dashboard</h2>
        <LoadingState message="Loading dashboard…" />
      </section>
    )
  }

  const orderMeta = orders.meta as OrderListMeta | undefined
  const revenue = orderMeta?.revenue ?? 0
  const recentOrders = orders.data ?? []

  return (
    <section className="space-y-6">
      <h2 className="text-2xl font-bold">Dashboard</h2>
      <div className="grid gap-4 md:grid-cols-3">
        <div className="stats stats-vertical border border-base-300 bg-base-100 shadow-sm">
          <div className="stat">
            <div className="stat-title">Sales — last 30 days</div>
            <div className="stat-value">EUR {revenue.toFixed(2)}</div>
            <div className="stat-desc">Revenue from placed orders</div>
          </div>
        </div>
        <div className="stats stats-vertical border border-base-300 bg-base-100 shadow-sm">
          <div className="stat">
            <div className="stat-title">Orders — last 30 days</div>
            <div className="stat-value">{orders.total ?? 0}</div>
            <div className="stat-desc">Orders placed in this period</div>
          </div>
        </div>
        <div className="stats stats-vertical border border-base-300 bg-base-100 shadow-sm">
          <div className="stat">
            <div className="stat-title">New customers — last 30 days</div>
            <div className="stat-value">{customers.total ?? 0}</div>
            <div className="stat-desc">Customer profiles created in this period</div>
          </div>
        </div>
      </div>

      <div className="card border border-base-300 bg-base-100 shadow-sm">
        <div className="card-body">
          <h3 className="card-title">Recent orders</h3>
          {recentOrders.length === 0 ? (
            <EmptyState title="No orders in the last 30 days" />
          ) : (
            <div className="overflow-x-auto">
              <table className="table table-zebra">
                <thead>
                  <tr>
                    <th>Order number</th>
                    <th>Customer</th>
                    <th>Total</th>
                    <th>Placed</th>
                  </tr>
                </thead>
                <tbody>
                  {recentOrders.map((order) => (
                    <tr key={order.id}>
                      <td>
                        <Link to={createPath({resource: 'orders', id: order.id, type: 'show'})}>
                          {order.orderNumber}
                        </Link>
                      </td>
                      <td>{order.customerId}</td>
                      <td>EUR {order.total.toFixed(2)}</td>
                      <td>{new Date(order.placedAt).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </section>
  )
}
