export function DashboardPage() {
  return (
    <section className="space-y-4">
      <h2 className="text-2xl font-bold">Dashboard</h2>
      <div className="grid gap-4 md:grid-cols-3">
        <div className="card bg-base-100 shadow-sm">
          <div className="card-body">
            <h3 className="card-title text-sm">Sales</h3>
            <p className="text-3xl font-bold">$58.4K</p>
          </div>
        </div>
        <div className="card bg-base-100 shadow-sm">
          <div className="card-body">
            <h3 className="card-title text-sm">Orders</h3>
            <p className="text-3xl font-bold">328</p>
          </div>
        </div>
        <div className="card bg-base-100 shadow-sm">
          <div className="card-body">
            <h3 className="card-title text-sm">Customers</h3>
            <p className="text-3xl font-bold">1,204</p>
          </div>
        </div>
      </div>
    </section>
  )
}
