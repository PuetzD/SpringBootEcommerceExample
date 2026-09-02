import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {ApiError} from '../api/client'
import type {Category, PageResponse} from '../api/types'

const {listCategories, getCategory, createCategory, renameCategory, deleteCategory} = vi.hoisted(() => ({
  listCategories: vi.fn(),
  getCategory: vi.fn(),
  createCategory: vi.fn(),
  renameCategory: vi.fn(),
  deleteCategory: vi.fn(),
}))

vi.mock('../api/catalog', () => ({
  listCategories,
  getCategory,
  createCategory,
  renameCategory,
  deleteCategory,
}))

import {CategoriesPage} from './CategoriesPage'

const category: Category = {
  id: 4,
  name: 'Tools',
  slug: 'tools',
  revision: 3,
  productCount: 2,
}

const page = (content: Category[] = [category]): PageResponse<Category> => ({
  content,
  page: 0,
  size: 20,
  totalElements: content.length,
  totalPages: 1,
})

describe('CategoriesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listCategories.mockResolvedValue(page())
  })

  it('loads the first server page and renders semantic category columns', async () => {
    render(<CategoriesPage/>)

    await waitFor(() => expect(listCategories).toHaveBeenCalledWith({page: 0, size: 20}))
    for (const heading of ['Name', 'Slug', 'Products', 'Actions']) {
      expect(screen.getByRole('columnheader', {name: heading})).toBeTruthy()
    }
    expect(screen.getByText('Tools')).toBeTruthy()
    expect(screen.getByText('2')).toBeTruthy()
  })

  it('reports loading, request failure, and an empty state with a create action', async () => {
    let resolveList: (value: PageResponse<Category>) => void
    listCategories.mockImplementationOnce(
      () =>
        new Promise((resolve) => {
          resolveList = resolve
        }),
    )
    render(<CategoriesPage/>)
    expect(screen.getByRole('status').textContent).toMatch(/loading categories/i)
    resolveList!(page([]))

    await waitFor(() => expect(screen.getByText(/no categories found/i)).toBeTruthy())
    expect(screen.getAllByRole('button', {name: /create category/i}).length).toBeGreaterThan(0)

    listCategories.mockRejectedValueOnce(
      new ApiError({status: 500, code: 'internal.error', message: 'nope', fieldErrors: []}),
    )
    fireEvent.click(screen.getByRole('button', {name: /refresh categories/i}))
    await waitFor(() =>
      expect(screen.getByRole('alert').textContent).toMatch(/unable to load categories/i),
    )
  })

  it('creates a category and shows the server-generated slug', async () => {
    createCategory.mockResolvedValue({...category, id: 5, name: 'Garden', slug: 'garden'})
    render(<CategoriesPage/>)
    await screen.findByText('Tools')

    fireEvent.click(screen.getAllByRole('button', {name: /create category/i})[0])
    fireEvent.change(screen.getByLabelText(/^name$/i), {target: {value: 'Garden'}})
    fireEvent.click(screen.getAllByRole('button', {name: /create category/i}).at(-1)!)

    await waitFor(() => expect(createCategory).toHaveBeenCalledWith({name: 'Garden'}))
    expect(await screen.findByText('garden')).toBeTruthy()
  })

  it('renames a category with the current revision and updates its row', async () => {
    renameCategory.mockResolvedValue({...category, name: 'Garden tools', slug: 'garden-tools', revision: 4})
    render(<CategoriesPage/>)
    await screen.findByText('Tools')

    fireEvent.click(screen.getByRole('button', {name: /rename tools/i}))
    fireEvent.change(screen.getByLabelText(/^name$/i), {target: {value: 'Garden tools'}})
    fireEvent.click(screen.getByRole('button', {name: /save changes/i}))

    await waitFor(() =>
      expect(renameCategory).toHaveBeenCalledWith(4, {name: 'Garden tools', revision: 3}),
    )
    expect(await screen.findByText('garden-tools')).toBeTruthy()
  })

  it('delegates bounded pagination to the server', async () => {
    listCategories.mockResolvedValue({...page(), totalPages: 2})
    render(<CategoriesPage/>)
    await screen.findByText('Tools')

    fireEvent.click(screen.getByRole('button', {name: '2'}))

    await waitFor(() => expect(listCategories).toHaveBeenLastCalledWith({page: 1, size: 20}))
  })

  it('keeps validation field errors in the editor', async () => {
    createCategory.mockRejectedValue(
      new ApiError({
        status: 400,
        code: 'request.validation',
        message: 'Validation failed',
        fieldErrors: [{field: 'name', message: 'Name is required'}],
      }),
    )
    render(<CategoriesPage/>)
    await screen.findByText('Tools')

    fireEvent.click(screen.getAllByRole('button', {name: /create category/i})[0])
    fireEvent.change(screen.getByLabelText(/^name$/i), {target: {value: 'Tools'}})
    fireEvent.click(screen.getAllByRole('button', {name: /create category/i}).at(-1)!)

    expect(await screen.findByText('Name is required')).toBeTruthy()
    expect(screen.getByRole('dialog')).toBeTruthy()
  })

  it('requires confirmation before deleting with the row revision', async () => {
    deleteCategory.mockResolvedValue(undefined)
    render(<CategoriesPage/>)
    await screen.findByText('Tools')

    fireEvent.click(screen.getByRole('button', {name: /delete tools/i}))
    expect(deleteCategory).not.toHaveBeenCalled()
    fireEvent.click(screen.getByRole('button', {name: /delete category/i}))

    await waitFor(() => expect(deleteCategory).toHaveBeenCalledWith(4, 3))
    expect(screen.queryByText('Tools')).toBeNull()
  })

  it('keeps an in-use category and explains that product memberships must be removed', async () => {
    deleteCategory.mockRejectedValue(
      new ApiError({
        status: 409,
        code: 'catalog.category.in-use',
        message: 'Category in use',
        fieldErrors: [],
      }),
    )
    render(<CategoriesPage/>)
    await screen.findByText('Tools')

    fireEvent.click(screen.getByRole('button', {name: /delete tools/i}))
    fireEvent.click(screen.getByRole('button', {name: /delete category/i}))

    expect((await screen.findByRole('alert')).textContent).toMatch(
      /remove this category from every product/i,
    )
    expect(screen.getByText('Tools')).toBeTruthy()
  })

  it('refreshes a stale category instead of retrying the delete', async () => {
    deleteCategory.mockRejectedValue(
      new ApiError({
        status: 409,
        code: 'catalog.category.stale',
        message: 'Category changed',
        fieldErrors: [],
      }),
    )
    getCategory.mockResolvedValue({...category, name: 'Updated elsewhere', revision: 4})
    render(<CategoriesPage/>)
    await screen.findByText('Tools')

    fireEvent.click(screen.getByRole('button', {name: /delete tools/i}))
    fireEvent.click(screen.getByRole('button', {name: /delete category/i}))

    await waitFor(() => expect(getCategory).toHaveBeenCalledWith(4))
    expect(deleteCategory).toHaveBeenCalledTimes(1)
    expect(await screen.findByText('Updated elsewhere')).toBeTruthy()
  })
})
