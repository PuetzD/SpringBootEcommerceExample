import type { ApiErrorResponse, FieldErrorResponse, PageResponse } from './types'
import { clearToken, getToken, refreshToken } from '../auth/CsrfProvider'

export class ApiError extends Error {
    status: number
    fieldErrors: FieldErrorResponse[]
    constructor(body: ApiErrorResponse) {
        super(body.message)
        this.status = body.status
        this.fieldErrors = body.fieldErrors ?? []
    }
}

function csrfHeaders(): Record<string, string> {
    const token = getToken()
    return token ? { 'X-XSRF-TOKEN': token } : {}
}

async function parseResponse<T>(response: Response): Promise<T> {
    const contentType = response.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
        return (await response.json()) as T
    }
    return undefined as T
}

async function handleResponse<T>(response: Response): Promise<T> {
    if (response.ok) {
        return parseResponse<T>(response)
    }
    const errorBody = (await parseResponse<ApiErrorResponse>(response)) ?? {
        message: 'Request failed',
        status: response.status,
    }
    if (response.status === 401 || response.status === 403) {
        clearToken()
        await refreshToken()
    }
    throw new ApiError(errorBody)
}

export const ApiClient = {
    async get<T>(path: string, params?: Record<string, string | number>): Promise<T> {
        const url = new URL(path, window.location.origin)
        if (params) {
            Object.entries(params).forEach(([key, value]) => {
                url.searchParams.append(key, String(value))
            })
        }
        const response = await fetch(url.toString(), {
            method: 'GET',
            credentials: 'same-origin',
            headers: { Accept: 'application/json', ...csrfHeaders() },
        })
        return handleResponse<T>(response)
    },

    async post<T>(path: string, body: unknown): Promise<T> {
        const response = await fetch(path, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json',
                ...csrfHeaders(),
            },
            body: JSON.stringify(body),
        })
        return handleResponse<T>(response)
    },

    async put<T>(path: string, body: unknown): Promise<T> {
        const response = await fetch(path, {
            method: 'PUT',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json',
                ...csrfHeaders(),
            },
            body: JSON.stringify(body),
        })
        return handleResponse<T>(response)
    },

    async delete(path: string): Promise<void> {
        const response = await fetch(path, {
            method: 'DELETE',
            credentials: 'same-origin',
            headers: { Accept: 'application/json', ...csrfHeaders() },
        })
        await handleResponse<void>(response)
    },
}
