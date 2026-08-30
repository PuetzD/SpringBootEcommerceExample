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
