import {HttpError, type DataProvider, type GetListParams} from 'react-admin'
import {ApiClient, ApiError} from '../api/client'
import type {
  Category,
  CategorySummary,
  Customer,
  CreateCategoryInput,
  CreateProductInput,
  PageResponse,
  Product,
  UpdateProductInput,
  Order,
} from '../api/types'

type CatalogResource = 'products' | 'categories' | 'orders' | 'customers'

type ProductMutationData = Partial<Product> & {
  categoryIds?: number[]
  categories?: CategorySummary[]
  sku?: string
}

type CategoryMutationData = Partial<Category>
type ProductRecord = Product & {categoryIds: number[]}

const resourcePaths: Record<CatalogResource, string> = {
  products: '/api/admin/products',
  categories: '/api/admin/categories',
  orders: '/api/admin/orders',
  customers: '/api/admin/customers',
}

function unsupportedMethod(method: string) {
  return async () => {
    throw new Error(`Unsupported react-admin method: ${method}`)
  }
}

function resourcePath(resource: string): string {
  if (resource in resourcePaths) {
    return resourcePaths[resource as CatalogResource]
  }
  throw new Error(`Unsupported catalog resource: ${resource}`)
}

function normalizeRecord<RecordType extends {id: number | string}>(record: RecordType): RecordType {
  return {...record}
}

function normalizeProductRecord(record: Product): ProductRecord {
  return {
    ...record,
    categoryIds: record.categories.map((category) => category.id),
  }
}

function normalizeList<RecordType extends {id: number | string}>(
  response: PageResponse<RecordType>,
) {
  return {
    data: response.content.map(normalizeRecord),
    total: response.totalElements,
  }
}

function normalizeStringFilter(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim() !== '' ? value.trim() : undefined
}

function normalizeBooleanFilter(value: unknown): boolean | undefined {
  if (typeof value === 'boolean') {
    return value
  }
  if (value === 'true') {
    return true
  }
  if (value === 'false') {
    return false
  }
  return undefined
}

function normalizeProductListParams(params: GetListParams) {
  const page = params.pagination?.page ?? 1
  const perPage = params.pagination?.perPage ?? 20

  return {
    page: Math.max(0, page - 1),
    size: perPage,
    q: normalizeStringFilter(params.filter?.q),
    active: normalizeBooleanFilter(params.filter?.active),
  }
}

function normalizeCategoryListParams(params: GetListParams) {
  const page = params.pagination?.page ?? 1
  const perPage = params.pagination?.perPage ?? 20

  return {
    page: Math.max(0, page - 1),
    size: perPage,
  }
}

function normalizeOrderListParams(params: GetListParams) {
  const page = params.pagination?.page ?? 1
  const perPage = params.pagination?.perPage ?? 20

  return {
    page: Math.max(0, page - 1),
    size: perPage,
    q: normalizeStringFilter(params.filter?.q),
  }
}

function normalizeCustomerListParams(params: GetListParams) {
  const page = params.pagination?.page ?? 1
  const perPage = params.pagination?.perPage ?? 20

  return {
    page: Math.max(0, page - 1),
    size: perPage,
    q: normalizeStringFilter(params.filter?.q),
  }
}

function requireString(value: string | undefined, field: string): string {
  if (value === undefined || value.trim() === '') {
    throw new Error(`${field} is required`)
  }
  return value
}

function requireNumber(value: number | undefined, field: string): number {
  if (value === undefined) {
    throw new Error(`${field} is required`)
  }
  return value
}

function requireBoolean(value: boolean | undefined, field: string): boolean {
  if (value === undefined) {
    throw new Error(`${field} is required`)
  }
  return value
}

function categoryIds(data: ProductMutationData): number[] {
  if (Array.isArray(data.categoryIds)) {
    return data.categoryIds
  }
  if (Array.isArray(data.categories)) {
    return data.categories.map((category) => category.id)
  }
  return []
}

function resolveRevision(
  data: {revision?: number},
  previousData?: {revision?: number},
): number {
  const revision = data.revision ?? previousData?.revision
  if (revision === undefined) {
    throw new Error('revision is required')
  }
  return revision
}

function toCreateProductInput(data: ProductMutationData): CreateProductInput {
  return {
    sku: requireString(data.sku, 'sku'),
    name: requireString(data.name, 'name'),
    description: data.description ?? null,
    price: requireNumber(data.price, 'price'),
    stockQuantity: requireNumber(data.stockQuantity, 'stockQuantity'),
    imageUrl: data.imageUrl ?? null,
    categoryIds: categoryIds(data),
  }
}

function toUpdateProductInput(
  data: ProductMutationData,
  revision: number,
): UpdateProductInput {
  return {
    revision,
    name: requireString(data.name, 'name'),
    description: data.description ?? null,
    price: requireNumber(data.price, 'price'),
    stockQuantity: requireNumber(data.stockQuantity, 'stockQuantity'),
    imageUrl: data.imageUrl ?? null,
    active: requireBoolean(data.active, 'active'),
    categoryIds: categoryIds(data),
  }
}

function toReactAdminError(error: unknown) {
  if (!(error instanceof ApiError)) {
    return error
  }

  const fieldErrors =
    error.fieldErrors.length === 0
      ? undefined
      : Object.fromEntries(error.fieldErrors.map(({field, message}) => [field, message]))

  const body = {
    message: error.message,
    status: error.status,
    code: error.code,
    fieldErrors: error.fieldErrors,
    errors: fieldErrors,
    retryable: false,
  }

  const httpError = new HttpError(error.message, error.status, body)
  return Object.assign(httpError, {
    code: error.code,
    fieldErrors: error.fieldErrors,
    retryable: false,
  })
}

async function runWithReactAdminError<T>(request: () => Promise<T>): Promise<T> {
  try {
    return await request()
  } catch (error) {
    throw toReactAdminError(error)
  }
}

export const dataProvider = {
  async getList(resource, params) {
    const path = resourcePath(resource)

    if (resource === 'products') {
      const response = await runWithReactAdminError(() =>
        ApiClient.get<PageResponse<Product>>(path, {params: normalizeProductListParams(params)}),
      )
      return {
        data: response.content.map(normalizeProductRecord),
        total: response.totalElements,
      }
    }

    if (resource === 'orders') {
      const response = await runWithReactAdminError(() =>
        ApiClient.get<PageResponse<Order>>(path, {params: normalizeOrderListParams(params)}),
      )
      return normalizeList(response)
    }

    if (resource === 'customers') {
      const response = await runWithReactAdminError(() =>
        ApiClient.get<PageResponse<Customer>>(path, {params: normalizeCustomerListParams(params)}),
      )
      return normalizeList(response)
    }

    const response = await runWithReactAdminError(() =>
      ApiClient.get<PageResponse<Category>>(path, {params: normalizeCategoryListParams(params)}),
    )
    return normalizeList(response)
  },

  async getOne(resource, params) {
    const path = resourcePath(resource)

    if (resource === 'products') {
      const response = await runWithReactAdminError(() =>
        ApiClient.get<Product>(`${path}/${params.id}`),
      )
      return {data: normalizeProductRecord(response)}
    }

    if (resource === 'orders') {
      const response = await runWithReactAdminError(() =>
        ApiClient.get<Order>(`${path}/${params.id}`),
      )
      return {data: normalizeRecord(response)}
    }

    if (resource === 'customers') {
      const response = await runWithReactAdminError(() =>
        ApiClient.get<Customer>(`${path}/${params.id}`),
      )
      return {data: normalizeRecord(response)}
    }

    const response = await runWithReactAdminError(() =>
      ApiClient.get<Category>(`${path}/${params.id}`),
    )
    return {data: normalizeRecord(response)}
  },

  async getMany(resource, params) {
    if (resource === 'orders' || resource === 'customers') {
      throw new Error(`Unsupported react-admin method: getMany for ${resource}`)
    }
    const path = resourcePath(resource)
    const records = await runWithReactAdminError(() =>
      Promise.all(params.ids.map((id) => ApiClient.get<Product | Category>(`${path}/${id}`))),
    )

    return {
      data:
        resource === 'products'
          ? records.map((record) => normalizeProductRecord(record as Product))
          : records.map((record) => normalizeRecord(record as Category)),
    }
  },
  getManyReference: unsupportedMethod('getManyReference'),

  async update(resource, params) {
    if (resource === 'orders' || resource === 'customers') {
      throw new Error(`Unsupported react-admin method: update for ${resource}`)
    }
    const path = resourcePath(resource)

    if (resource === 'products') {
      const data = params.data as ProductMutationData
      const previousData = params.previousData as ProductMutationData
      const revision = resolveRevision(data, previousData)
      const response = await runWithReactAdminError(() =>
        ApiClient.put<Product>(
          `${path}/${params.id}`,
          toUpdateProductInput(data, revision),
          {revision},
        ),
      )
      return {data: normalizeProductRecord(response)}
    }

    const data = params.data as CategoryMutationData
    const previousData = params.previousData as CategoryMutationData
    const revision = resolveRevision(data, previousData)
    const response = await runWithReactAdminError(() =>
      ApiClient.put<Category>(
        `${path}/${params.id}`,
        {name: requireString(data.name, 'name')},
        {revision},
      ),
    )
    return {data: normalizeRecord(response)}
  },

  updateMany: unsupportedMethod('updateMany'),

  async create(resource, params) {
    if (resource === 'orders' || resource === 'customers') {
      throw new Error(`Unsupported react-admin method: create for ${resource}`)
    }
    const path = resourcePath(resource)

    if (resource === 'products') {
      const response = await runWithReactAdminError(() =>
        ApiClient.post<Product>(path, toCreateProductInput(params.data as ProductMutationData)),
      )
      return {data: normalizeProductRecord(response)}
    }

    const data = params.data as CreateCategoryInput
    const response = await runWithReactAdminError(() =>
      ApiClient.post<Category>(path, {name: requireString(data.name, 'name')}),
    )
    return {data: normalizeRecord(response)}
  },

  async delete(resource, params) {
    if (resource === 'orders' || resource === 'customers') {
      throw new Error(`Unsupported react-admin method: delete for ${resource}`)
    }
    const path = resourcePath(resource)
    const previousData = params.previousData as ProductMutationData | CategoryMutationData | undefined
    const revision = resolveRevision(previousData ?? {})

    await runWithReactAdminError(() =>
      ApiClient.delete(`${path}/${params.id}`, {revision}),
    )

    return {
      data: normalizeRecord((params.previousData ?? {id: params.id}) as {id: number}),
    }
  },

  deleteMany: unsupportedMethod('deleteMany'),
} as DataProvider
