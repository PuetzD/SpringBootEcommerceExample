type PaginationProps = {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) {
    return null
  }

  const visiblePages = Array.from({ length: totalPages }, (_, index) => index + 1)

  return (
    <div className="join mt-4 justify-center">
      {visiblePages.map((pageNumber) => (
        <button
          key={pageNumber}
          type="button"
          className={`join-item btn btn-sm ${page === pageNumber ? 'btn-primary' : 'btn-ghost'}`}
          aria-current={page === pageNumber ? 'page' : undefined}
          onClick={() => onPageChange(pageNumber)}
        >
          {pageNumber}
        </button>
      ))}
    </div>
  )
}
