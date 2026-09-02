import {HttpError} from 'react-admin'
import {ApiError} from '../api/client'
import type {Category, PageResponse, Product} from '../api/types'

const {get, post, put, remove} = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  remove: vi.fn(),
}))

vi.mock('../api/client', async () => {
  const actual = await vi.importActual<typeof import('../api/client')>('../api/client')

  return {
    ...actual,
    ApiClient: {
      get,
      post,
      put,
      delete: remove,
    },
  }
})

import {dataProvider} from './dataProvider'

const product: Product = {
  id: 9,
  sku: 'SKU-9',
  name: 'Router',
  description: 'Mesh router',
  price: 199.99,
  stockQuantity: 8,
  imageUrl: null,
  active: true,
  revision: 4,
  categories: [{id: 3, name: 'Networking', slug: 'networking'}],
}

const category: Category = {
  id: 7,
  name: 'Networking',
  slug: 'networking',
  revision: 2,
  productCount: 4,
}

const normalizedProduct = {
  ...product,
  categoryIds: [3],
}

describe('dataProvider', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('normalizes Spring page responses and product filters for getList', async () => {
    const response: PageResponse<Product> = {
      content: [product],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
    }
    get.mockResolvedValue(response)

    await expect(
      dataProvider.getList('products', {
        pagination: {page: 2, perPage: 20},
        sort: {field: 'name', order: 'ASC'},
        filter: {q: ' router ', active: 'false'},
      }),
    ).resolves.toEqual({
      data: [normalizedProduct],
      total: 1,
    })

    expect(get).toHaveBeenCalledWith('/api/admin/products', {
      params: {
        page: 1,
        size: 20,
        q: 'router',
        active: false,
      },
    })
  })

  it('normalizes product categoryIds for getOne while preserving backend category data', async () => {
    get.mockResolvedValue(product)

    await expect(
      dataProvider.getOne('products', {
        id: product.id,
      }),
    ).resolves.toEqual({
      data: normalizedProduct,
    })
  })

  it('normalizes product categoryIds for create responses', async () => {
    post.mockResolvedValue(product)

    await expect(
      dataProvider.create('products', {
        data: {
          ...product,
          categoryIds: [3],
        },
      }),
    ).resolves.toEqual({data: normalizedProduct})
  })

  it('updates products with body revision and the shared If-Match transport option', async () => {
    put.mockResolvedValue(product)

    await expect(
      dataProvider.update('products', {
        id: product.id,
        data: {
          ...product,
          name: 'Router Pro',
          categoryIds: [3],
        },
        previousData: product,
      }),
    ).resolves.toEqual({data: normalizedProduct})

    expect(put).toHaveBeenCalledWith(
      '/api/admin/products/9',
      {
        revision: 4,
        name: 'Router Pro',
        description: 'Mesh router',
        price: 199.99,
        stockQuantity: 8,
        imageUrl: null,
        active: true,
        categoryIds: [3],
      },
      {revision: 4},
    )
  })

  it('updates categories with the shared If-Match transport option and a rename body', async () => {
    put.mockResolvedValue(category)

    await expect(
      dataProvider.update('categories', {
        id: category.id,
        data: {...category, name: 'Wi-Fi'},
        previousData: category,
      }),
    ).resolves.toEqual({data: category})

    expect(put).toHaveBeenCalledWith(
      '/api/admin/categories/7',
      {name: 'Wi-Fi'},
      {revision: 2},
    )
  })

  it('deletes resources with the shared If-Match transport option and returns previous data', async () => {
    remove.mockResolvedValue(undefined)

    await expect(
      dataProvider.delete('categories', {
        id: category.id,
        previousData: category,
      }),
    ).resolves.toEqual({data: category})

    expect(remove).toHaveBeenCalledWith('/api/admin/categories/7', {revision: 2})
  })

  it.each([
    ['catalog.product.stale'],
    ['catalog.category.stale'],
    ['catalog.category.in-use'],
  ])('maps %s conflicts to non-retrying React-Admin errors', async (code) => {
    remove.mockRejectedValue(
      new ApiError({
        status: 409,
        code,
        message: 'Conflict',
        fieldErrors: {},
      }),
    )

    await expect(
      dataProvider.delete('categories', {
        id: category.id,
        previousData: category,
      }),
    ).rejects.toMatchObject({
      status: 409,
      code,
      retryable: false,
      body: expect.objectContaining({
        status: 409,
        code,
        retryable: false,
      }),
    })

    await expect(
      dataProvider.delete('categories', {
        id: category.id,
        previousData: category,
      }),
    ).rejects.toBeInstanceOf(HttpError)
  })
})
