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

export interface OrderItem {
    productId: number
    sku: string
    productName: string
    unitPrice: number
    quantity: number
    lineTotal: number
}

export interface OrderAddress {
    role: string
    recipientName: string
    companyName: string | null
    addressLine1: string
    addressLine2: string | null
    city: string
    region: string | null
    postalCode: string
    countryCode: string
    phoneNumber: string | null
}

export interface Order {
    id: string
    orderId: string
    orderNumber: string
    customerId: number
    total: number
    placedAt: string
    items: OrderItem[]
    addresses: OrderAddress[]
}

export interface CustomerAddress {
    id: number
    recipientName: string
    companyName: string | null
    addressLine1: string
    addressLine2: string | null
    city: string
    region: string | null
    postalCode: string
    countryCode: string
    phoneNumber: string | null
    defaultShipping: boolean
    defaultBilling: boolean
}

export interface CustomerOrder {
    orderNumber: string
    orderId: string
    total: number
    placedAt: string
    orderUrl: string
}

export interface Customer {
    id: number
    givenName: string
    familyName: string
    contactEmail: string
    accountId: number | null
    addresses: CustomerAddress[]
    orders: CustomerOrder[]
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
