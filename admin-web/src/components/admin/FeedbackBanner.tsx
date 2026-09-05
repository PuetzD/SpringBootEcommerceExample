type FeedbackBannerProps = {
  title?: string
  message?: string
  tone?: 'info' | 'success' | 'warning' | 'error'
}

export function FeedbackBanner({
  title = 'System status',
  message = 'All systems operational.',
  tone = 'success',
}: FeedbackBannerProps) {
  return (
    <div className={`alert alert-${tone} mb-4`} role={tone === 'error' ? 'alert' : 'status'}>
      <div>
        <h2 className="font-semibold">{title}</h2>
        <p>{message}</p>
      </div>
    </div>
  )
}
