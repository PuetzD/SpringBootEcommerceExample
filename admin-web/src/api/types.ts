export interface ApiErrorResponse {
    message: string
    status: number
    fieldErrors?: FieldErrorResponse[]
}

export interface FieldErrorResponse {
    field: string
    message: string
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
