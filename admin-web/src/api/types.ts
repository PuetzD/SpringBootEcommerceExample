export interface ApiErrorResponse {
    message: string
    status: number
    code?: string
    fieldErrors?: FieldErrorResponse[] | Record<string, string>
}

export interface FieldErrorResponse {
    field: string
    message: string
}

export type ApiQueryValue = string | number | boolean

export type ApiQueryParams = Record<string, ApiQueryValue | undefined>

export interface ApiRequestOptions {
    params?: ApiQueryParams
    revision?: number
}

export interface PageResponse<T> {
    content: T[]
    page: number
    size: number
    totalElements: number
    totalPages: number
}

export interface CategorySummary {
    id: number
    name: string
    slug: string
}

export interface Product {
    id: number
    sku: string
    name: string
    description: string | null
    price: number
    stockQuantity: number
    imageUrl: string | null
    active: boolean
    revision: number
    categories: CategorySummary[]
}

export interface CategoryOption {
    id: number
    name: string
    slug: string
}

export interface Category {
    id: number
    name: string
    slug: string
    revision: number
    productCount: number
}

export interface CreateCategoryInput {
    name: string
}

export interface RenameCategoryInput {
    name: string
    revision: number
}

export interface CreateProductInput {
    sku: string
    name: string
    description: string | null
    price: number
    stockQuantity: number
    imageUrl: string | null
    categoryIds: number[]
}

export interface UpdateProductInput {
    revision: number
    name: string
    description: string | null
    price: number
    stockQuantity: number
    imageUrl: string | null
    active: boolean
    categoryIds: number[]
}
