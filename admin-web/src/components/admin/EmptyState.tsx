type EmptyStateProps = {
  title: string
  description: string
}

export function EmptyState({ title, description }: EmptyStateProps) {
  return (
    <div className="rounded-box border border-dashed border-base-300 bg-base-100 p-8 text-center shadow-sm">
      <p className="text-lg font-semibold">{title}</p>
      <p className="mt-2 text-sm text-base-content/70">{description}</p>
    </div>
  )
}
