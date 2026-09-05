import {ApiClient} from './client'
import type {
    Category,
    CategoryOption,
    CreateCategoryInput,
    CreateProductInput,
    PageResponse,
    Product,
    RenameCategoryInput,
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
    return ApiClient.put(`/api/admin/products/${id}`, input, input.revision)
}

export function deactivateProduct(id: number, revision: number): Promise<void> {
    return ApiClient.delete(`/api/admin/products/${id}`, revision)
}

export function listCategoryOptions(): Promise<CategoryOption[]> {
    return ApiClient.get('/api/admin/categories/options')
}

export function listCategories(params: {page: number; size: number}): Promise<PageResponse<Category>> {
    return ApiClient.get('/api/admin/categories', params)
}

export function getCategory(id: number): Promise<Category> {
    return ApiClient.get(`/api/admin/categories/${id}`)
}

export function createCategory(input: CreateCategoryInput): Promise<Category> {
    return ApiClient.post('/api/admin/categories', input)
}

export function renameCategory(id: number, input: RenameCategoryInput): Promise<Category> {
    return ApiClient.put(`/api/admin/categories/${id}`, {name: input.name}, input.revision)
}

export function deleteCategory(id: number, revision: number): Promise<void> {
    return ApiClient.delete(`/api/admin/categories/${id}`, revision)
}
