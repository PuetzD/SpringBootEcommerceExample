import {ApiClient} from './client'
import type {
    CategoryOption,
    CreateProductInput,
    PageResponse,
    Product,
    UpdateProductInput,
} from './types'

export function listProducts(params: {
    page: number
    size: number
    q?: string
    active?: boolean
}): Promise<PageResponse<Product>> {
    return ApiClient.get('/api/admin/products', params)
}

export function getProduct(id: number): Promise<Product> {
    return ApiClient.get(`/api/admin/products/${id}`)
}

export function createProduct(input: CreateProductInput): Promise<Product> {
    return ApiClient.post('/api/admin/products', input)
}

export function updateProduct(id: number, input: UpdateProductInput): Promise<Product> {
    return ApiClient.put(`/api/admin/products/${id}`, input)
}

export function deactivateProduct(id: number, revision: number): Promise<void> {
    void revision
    return ApiClient.delete(`/api/admin/products/${id}`)
}

export function listCategoryOptions(): Promise<CategoryOption[]> {
    return ApiClient.get('/api/admin/categories/options')
}
