type LoadingStateProps = {
  message?: string
}

export function LoadingState({ message = 'Loading…' }: LoadingStateProps) {
  return (
    <div className="flex items-center gap-3 rounded-box border border-base-300 bg-base-100 p-4 text-base-content/75" role="status" aria-live="polite">
      <span className="loading loading-spinner loading-md text-primary" aria-hidden="true" />
      <span>{message}</span>
    </div>
  )
}
