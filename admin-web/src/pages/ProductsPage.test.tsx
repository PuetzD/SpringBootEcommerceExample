import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { ApiError } from '../api/client'
import type { PageResponse, Product } from '../api/types'

const {
  listProducts,
  getProduct,
  createProduct,
  updateProduct,
  deactivateProduct,
  listCategoryOptions,
} = vi.hoisted(() => ({
  listProducts: vi.fn(),
  getProduct: vi.fn(),
  createProduct: vi.fn(),
  updateProduct: vi.fn(),
  deactivateProduct: vi.fn(),
  listCategoryOptions: vi.fn(),
}))

vi.mock('../api/catalog', () => ({
  listProducts,
  getProduct,
  createProduct,
  updateProduct,
  deactivateProduct,
  listCategoryOptions,
}))

import { ProductsPage } from './ProductsPage'

const product: Product = {
  id: 4,
  sku: 'BOOK-4',
  name: 'Book Four',
  description: null,
  price: 10.5,
  stockQuantity: 2,
  imageUrl: null,
  active: true,
  revision: 3,
  categories: [{ id: 1, name: 'Books', slug: 'books' }],
}

const page = (content: Product[] = [product]): PageResponse<Product> => ({
  content,
  page: 0,
  size: 20,
  totalElements: content.length,
  totalPages: 1,
})

describe('ProductsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listProducts.mockResolvedValue(page())
    listCategoryOptions.mockResolvedValue([{ id: 1, name: 'Books', slug: 'books' }])
  })

  it('loads page zero with the default page size and renders semantic columns', async () => {
    render(<ProductsPage />)

    await waitFor(() => expect(listProducts).toHaveBeenCalledWith({ page: 0, size: 20, q: undefined, active: undefined }))
    for (const heading of ['SKU', 'Product', 'Price', 'Stock', 'Status', 'Categories', 'Actions']) {
      expect(screen.getByRole('columnheader', { name: heading })).toBeTruthy()
    }
    expect(screen.getByText('BOOK-4')).toBeTruthy()
  })

  it('reports loading, request failure, and an empty state with a create action', async () => {
    let resolveList: (value: PageResponse<Product>) => void
    listProducts.mockImplementationOnce(() => new Promise((resolve) => { resolveList = resolve }))
    render(<ProductsPage />)
    expect(screen.getByRole('status').textContent).toMatch(/loading products/i)
    resolveList!(page([]))

    await waitFor(() => expect(screen.getByText(/no products found/i)).toBeTruthy())
    expect(screen.getAllByRole('button', { name: /create product/i }).length).toBeGreaterThan(0)

    listProducts.mockRejectedValueOnce(new ApiError({ status: 500, code: 'internal.error', message: 'nope', fieldErrors: {} }))
    fireEvent.click(screen.getByRole('button', { name: /refresh products/i }))
    await waitFor(() => expect(screen.getByRole('alert').textContent).toMatch(/unable to load products/i))
  })

  it('submits filters on page zero and shows inactive products when requested', async () => {
    render(<ProductsPage />)
    await screen.findByText('BOOK-4')
    fireEvent.change(screen.getByLabelText(/search products/i), { target: { value: 'book' } })
    fireEvent.change(screen.getByLabelText(/status/i), { target: { value: 'false' } })
    fireEvent.submit(screen.getByRole('button', { name: /apply filters/i }).closest('form')!)

    await waitFor(() => expect(listProducts).toHaveBeenLastCalledWith({ page: 0, size: 20, q: 'book', active: false }))
  })

  it('opens an editor, persists the returned product, and announces success', async () => {
    createProduct.mockResolvedValue({ ...product, id: 5, sku: 'NEW-5', name: 'New product' })
    render(<ProductsPage />)
    await screen.findByText('BOOK-4')
    fireEvent.click(screen.getAllByRole('button', { name: /create product/i })[0])
    await screen.findByRole('dialog')
    fireEvent.change(screen.getByLabelText(/sku/i), { target: { value: 'NEW-5' } })
    fireEvent.change(screen.getByLabelText(/^name/i), { target: { value: 'New product' } })
    fireEvent.change(screen.getByLabelText(/price/i), { target: { value: '2.50' } })
    fireEvent.change(screen.getByLabelText(/stock/i), { target: { value: '1' } })
    fireEvent.click(screen.getAllByRole('button', { name: /create product/i }).at(-1)!)

    await waitFor(() => expect(createProduct).toHaveBeenCalled())
    expect(await screen.findByText(/product created/i)).toBeTruthy()
  })

  it('refreshes and closes a stale editor without retrying the mutation', async () => {
    getProduct.mockResolvedValue({ ...product, name: 'Updated elsewhere', revision: 4 })
    updateProduct.mockRejectedValue(
      new ApiError({ status: 409, code: 'catalog.product.stale', message: 'stale', fieldErrors: {} }),
    )
    render(<ProductsPage />)
    await screen.findByText('BOOK-4')
    fireEvent.click(screen.getByRole('button', { name: /edit book four/i }))
    await screen.findByRole('dialog')
    fireEvent.click(screen.getByRole('button', { name: /save changes/i }))

    await waitFor(() => expect(getProduct).toHaveBeenCalledWith(4))
    expect(screen.queryByRole('dialog')).toBeNull()
    expect(updateProduct).toHaveBeenCalledTimes(1)
    expect(screen.getByText(/changed by someone else/i)).toBeTruthy()
  })

  it('requires confirmation before deactivation and sends the row revision', async () => {
    deactivateProduct.mockResolvedValue(undefined)
    render(<ProductsPage />)
    await screen.findByText('BOOK-4')
    fireEvent.click(screen.getByRole('button', { name: /deactivate book four/i }))
    expect(deactivateProduct).not.toHaveBeenCalled()
    fireEvent.click(screen.getByRole('button', { name: /^deactivate$/i }))

    await waitFor(() => expect(deactivateProduct).toHaveBeenCalledWith(4, 3))
    expect(screen.getByText(/product deactivated/i)).toBeTruthy()
  })
})
