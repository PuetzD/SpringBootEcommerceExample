type EmptyStateProps = {
  title: string
  description?: string
  actionLabel?: string
  onAction?: () => void
}

export function EmptyState({ title, description = '', actionLabel, onAction }: EmptyStateProps) {
  return (
    <div className="rounded-box border border-dashed border-base-300 bg-base-100 p-8 text-center shadow-sm">
      <p className="text-lg font-semibold">{title}</p>
      {description && <p className="mt-2 text-sm text-base-content/70">{description}</p>}
      {actionLabel && onAction && (
        <button type="button" className="btn btn-primary mt-4" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  )
}
