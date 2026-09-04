import { fireEvent, render, screen } from '@testing-library/react'
import { ConfirmDialog } from '../../../components/admin/ConfirmDialog'

describe('ConfirmDialog', () => {
  it('requires an explicit confirmation action', () => {
    const onConfirm = vi.fn()
    const onCancel = vi.fn()

    render(
      <ConfirmDialog
        open={true}
        title="Delete product"
        message="This action cannot be undone."
        confirmLabel="Delete product"
        cancelLabel="Cancel"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />,
    )

    fireEvent.click(screen.getByRole('button', { name: /delete product/i }))
    fireEvent.click(screen.getByRole('button', { name: /cancel/i }))

    expect(onConfirm).toHaveBeenCalledTimes(1)
    expect(onCancel).toHaveBeenCalledTimes(1)
  })
})
