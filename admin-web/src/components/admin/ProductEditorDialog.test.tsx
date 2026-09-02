import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { ProductEditorDialog } from './ProductEditorDialog'
import type { CategorySummary, Product } from '../../api/types'

const categories: CategorySummary[] = [
  { id: 1, name: 'Books', slug: 'books' },
  { id: 2, name: 'Games', slug: 'games' },
]

const product: Product = {
  id: 7,
  sku: 'BOOK-7',
  name: 'Book',
  description: 'A book',
  price: 12.5,
  stockQuantity: 3,
  imageUrl: 'https://example.test/book.jpg',
  active: false,
  revision: 4,
  categories: [categories[0]],
}

describe('ProductEditorDialog', () => {
  it('submits a create request with the selected category IDs', async () => {
    const onSubmit = vi.fn()
    render(
      <ProductEditorDialog open={true} categories={categories} onCancel={vi.fn()} onSubmit={onSubmit} />,
    )

    fireEvent.change(screen.getByLabelText(/sku/i), { target: { value: 'GAME-1' } })
    fireEvent.change(screen.getByLabelText(/^name/i), { target: { value: 'Game' } })
    fireEvent.change(screen.getByLabelText(/price/i), { target: { value: '19.99' } })
    fireEvent.change(screen.getByLabelText(/stock/i), { target: { value: '5' } })
    fireEvent.click(screen.getByLabelText('Games'))
    fireEvent.click(screen.getByRole('button', { name: /create product/i }))

    await waitFor(() =>
      expect(onSubmit).toHaveBeenCalledWith({
        sku: 'GAME-1',
        name: 'Game',
        description: null,
        price: 19.99,
        stockQuantity: 5,
        imageUrl: null,
        categoryIds: [2],
      }),
    )
  })

  it('shows editable product values, sends its revision, and keeps SKU read-only', async () => {
    const onSubmit = vi.fn()
    render(
      <ProductEditorDialog
        open={true}
        product={product}
        categories={categories}
        onCancel={vi.fn()}
        onSubmit={onSubmit}
      />,
    )

    expect(screen.getByLabelText(/sku/i)).toHaveProperty('readOnly', true)
    expect(screen.getByLabelText(/active/i)).toHaveProperty('checked', false)
    fireEvent.click(screen.getByLabelText(/active/i))
    fireEvent.click(screen.getByRole('button', { name: /save changes/i }))

    await waitFor(() =>
      expect(onSubmit).toHaveBeenCalledWith({
        revision: 4,
        name: 'Book',
        description: 'A book',
        price: 12.5,
        stockQuantity: 3,
        imageUrl: 'https://example.test/book.jpg',
        active: true,
        categoryIds: [1],
      }),
    )
  })

  it('announces required errors on their labelled inputs', async () => {
    render(<ProductEditorDialog open={true} categories={categories} onCancel={vi.fn()} onSubmit={vi.fn()} />)

    fireEvent.click(screen.getByRole('button', { name: /create product/i }))

    await waitFor(() => expect(screen.getByText('SKU is required')).toBeTruthy())
    expect(screen.getByLabelText(/sku/i).getAttribute('aria-invalid')).toBe('true')
    expect(screen.getByLabelText(/sku/i)).toBe(document.activeElement)
  })
})
