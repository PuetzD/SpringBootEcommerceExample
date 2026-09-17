import {HttpError} from 'react-admin'
import {ApiError} from '../../api/client'
import type {
  Category,
  CategoryOption,
  Customer,
  Order,
  OrderListMeta,
  PageResponse,
  Product,
  ProductVariant,
} from '../../api/types'

const {get, post, put, patch, remove} = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  patch: vi.fn(),
  remove: vi.fn(),
}))

vi.mock('../../api/client', async () => {
  const actual = await vi.importActual<typeof import('../../api/client')>('../../api/client')

  return {
    ...actual,
    ApiClient: {
      get,
      post,
      put,
      patch,
      delete: remove,
    },
  }
})

import {dataProvider} from '../../admin/dataProvider'

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

const order: Order = {
  id: 'ORD-2026-100001',
  orderId: '00000000-0000-0000-0000-000000000009',
  orderNumber: 'ORD-2026-100001',
  customerId: 7,
  total: 19.99,
  placedAt: '2026-09-05T09:00:00Z',
  items: [],
  addresses: [],
}

const customer: Customer = {
  id: 12,
  givenName: 'Alice',
  familyName: 'Example',
  contactEmail: 'alice@example.com',
  accountId: null,
  createdAt: '2026-09-10T09:00:00Z',
  addresses: [],
  orders: [],
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

  it('preserves order page metadata and forwards normalized reporting bounds', async () => {
    const response: PageResponse<Order, OrderListMeta> = {
      content: [order],
      page: 0,
      size: 5,
      totalElements: 3,
      totalPages: 1,
      meta: {revenue: 159.97, currency: 'EUR'},
    }
    get.mockResolvedValue(response)

    await expect(
      dataProvider.getList('orders', {
        pagination: {page: 2, perPage: 5},
        sort: {field: 'placedAt', order: 'DESC'},
        filter: {
          q: ' ORD-2026 ',
          from: ' 2026-08-18T12:00:00Z ',
          to: ' 2026-09-17T12:00:00Z ',
        },
      }),
    ).resolves.toEqual({
      data: [order],
      total: 3,
      meta: {revenue: 159.97, currency: 'EUR'},
    })

    expect(get).toHaveBeenCalledWith('/api/admin/orders', {
      params: {
        page: 1,
        size: 5,
        q: 'ORD-2026',
        from: '2026-08-18T12:00:00Z',
        to: '2026-09-17T12:00:00Z',
      },
    })
  })

  it('loads one order by order number', async () => {
    get.mockResolvedValue(order)

    await expect(dataProvider.getOne('orders', {id: order.orderNumber})).resolves.toEqual({
      data: order,
    })

    expect(get).toHaveBeenCalledWith(`/api/admin/orders/${order.orderNumber}`)
  })

  it('forwards normalized reporting bounds when loading customers', async () => {
    get.mockResolvedValue({content: [customer], totalElements: 1})

    await expect(
      dataProvider.getList('customers', {
        pagination: {page: 2, perPage: 10},
        sort: {field: 'familyName', order: 'ASC'},
        filter: {
          q: ' Alice ',
          from: ' 2026-08-18T12:00:00Z ',
          to: ' 2026-09-17T12:00:00Z ',
        },
      }),
    ).resolves.toEqual({data: [customer], total: 1})

    expect(get).toHaveBeenCalledWith('/api/admin/customers', {
      params: {
        page: 1,
        size: 10,
        q: 'Alice',
        from: '2026-08-18T12:00:00Z',
        to: '2026-09-17T12:00:00Z',
      },
    })
  })

  it('loads a customer detail by numeric id', async () => {
    get.mockResolvedValue(customer)

    await expect(dataProvider.getOne('customers', {id: 12})).resolves.toEqual({data: customer})

    expect(get).toHaveBeenCalledWith('/api/admin/customers/12')
  })

  it('keeps customer mutations unsupported', async () => {
    await expect(dataProvider.create('customers', {data: customer})).rejects.toThrow(
      'Unsupported react-admin method: create for customers',
    )
    await expect(dataProvider.update('customers', {id: 12, data: customer})).rejects.toThrow(
      'Unsupported react-admin method: update for customers',
    )
    await expect(dataProvider.delete('customers', {id: 12})).rejects.toThrow(
      'Unsupported react-admin method: delete for customers',
    )
    await expect(dataProvider.getMany('customers', {ids: [12]})).rejects.toThrow(
      'Unsupported react-admin method: getMany for customers',
    )
  })

  it('loads requested records for react-admin reference inputs', async () => {
    get.mockImplementation((path: string) =>
      Promise.resolve({...category, id: Number(path.split('/').pop())}),
    )

    await expect(
      dataProvider.getMany('categories', {ids: [3, 8]}),
    ).resolves.toEqual({
      data: [{...category, id: 3}, {...category, id: 8}],
    })

    expect(get).toHaveBeenNthCalledWith(1, '/api/admin/categories/3')
    expect(get).toHaveBeenNthCalledWith(2, '/api/admin/categories/8')
  })

  it('exposes every Category option through the provider reference resource', async () => {
    const options: CategoryOption[] = Array.from({length: 26}, (_, index) => ({
      id: index + 1,
      name: index === 25 ? 'Zulu' : `Category ${index + 1}`,
      slug: index === 25 ? 'zulu' : `category-${index + 1}`,
    }))
    get.mockResolvedValue(options)

    await expect(
      dataProvider.getList('categoryOptions', {
        pagination: {page: 1, perPage: 25},
        sort: {field: 'name', order: 'ASC'},
        filter: {},
      }),
    ).resolves.toEqual({data: options, total: 26})

    expect(get).toHaveBeenCalledWith('/api/admin/categories/options')
  })

  it('resolves selected Category options from the complete reference resource', async () => {
    const options: CategoryOption[] = [
      {id: 1, name: 'Alpha', slug: 'alpha'},
      {id: 26, name: 'Zulu', slug: 'zulu'},
    ]
    get.mockResolvedValue(options)

    await expect(dataProvider.getMany('categoryOptions', {ids: [26]})).resolves.toEqual({
      data: [{id: 26, name: 'Zulu', slug: 'zulu'}],
    })

    expect(get).toHaveBeenCalledWith('/api/admin/categories/options')
  })

  it('normalizes requested product records for react-admin reference inputs', async () => {
    get.mockImplementation((path: string) =>
      Promise.resolve({...product, id: Number(path.split('/').pop())}),
    )

    await expect(
      dataProvider.getMany('products', {ids: [9, 10]}),
    ).resolves.toEqual({
      data: [{...normalizedProduct, id: 9}, {...normalizedProduct, id: 10}],
    })

    expect(get).toHaveBeenNthCalledWith(1, '/api/admin/products/9')
    expect(get).toHaveBeenNthCalledWith(2, '/api/admin/products/10')
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

  it('loads product variants through the nested catalog endpoint', async () => {
    const variant = {
      id: 12,
      productId: 9,
      sku: 'SKU-9-BLUE',
      price: 209.99,
      stockQuantity: 4,
      imageUrl: null,
      active: true,
      defaultVariant: false,
      productRevision: 4,
    }
    get.mockResolvedValue([variant])

    await expect(
      dataProvider.getList('productVariants', {
        pagination: {page: 1, perPage: 100},
        filter: {productId: 9},
      }),
    ).resolves.toEqual({data: [variant], total: 1})

    expect(get).toHaveBeenCalledWith('/api/admin/products/9/variants')
  })

  it('updates a product variant with the product revision', async () => {
    const variant = {
      id: 12,
      productId: 9,
      sku: 'SKU-9-BLUE',
      price: 209.99,
      stockQuantity: 4,
      imageUrl: null,
      active: true,
      defaultVariant: false,
      productRevision: 5,
    }
    put.mockResolvedValue(variant)

    await expect(
      dataProvider.update('productVariants', {
        id: variant.id,
        data: {...variant, price: 219.99, revision: 4},
        previousData: variant,
      }),
    ).resolves.toEqual({data: variant})

    expect(put).toHaveBeenCalledWith(
      '/api/admin/products/9/variants/12',
      expect.objectContaining({revision: 4, price: 219.99}),
      {revision: 4},
    )
  })

  it('updates family fields without sending default commercial values', async () => {
    patch.mockResolvedValue(product)

    await expect(
      dataProvider.update('products', {
        id: product.id,
        data: {
          ...product,
          name: 'Family',
          price: 999,
          categoryIds: [3],
        },
        previousData: product,
      }),
    ).resolves.toEqual({data: normalizedProduct})

    expect(patch).toHaveBeenCalledWith(
      '/api/admin/products/9/family',
      {
        revision: 4,
        name: 'Family',
        description: 'Mesh router',
        active: true,
        categoryIds: [3],
      },
      {revision: 4},
    )
    expect(put).not.toHaveBeenCalled()
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

  it('deletes a variant using its response productRevision', async () => {
    const variant: ProductVariant = {
      id: 12,
      productId: 9,
      sku: 'BLUE',
      price: 20,
      stockQuantity: 4,
      imageUrl: null,
      active: true,
      defaultVariant: false,
      productRevision: 4,
    }
    remove.mockResolvedValue(undefined)

    await expect(
      dataProvider.delete('productVariants', {id: 12, previousData: variant}),
    ).resolves.toEqual({data: variant})

    expect(remove).toHaveBeenCalledWith('/api/admin/products/9/variants/12', {revision: 4})
  })

  it('rejects a missing variant revision before HTTP', async () => {
    const variant: ProductVariant = {
      id: 12,
      productId: 9,
      sku: 'BLUE',
      price: 20,
      stockQuantity: 4,
      imageUrl: null,
      active: true,
      defaultVariant: false,
      productRevision: 4,
    }
    const {productRevision, ...missingRevision} = variant
    expect(productRevision).toBe(4)

    await expect(
      dataProvider.delete('productVariants', {id: 12, previousData: missingRevision}),
    ).rejects.toThrow('revision is required')
    expect(remove).not.toHaveBeenCalled()
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
