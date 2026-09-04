import type {ApiErrorResponse, ApiQueryParams, ApiRequestOptions, FieldErrorResponse} from './types'
import {clearToken, getToken, refreshToken} from '../auth/CsrfProvider'

export class ApiError extends Error {
    status: number
    code: string
    fieldErrors: FieldErrorResponse[]

    constructor(body: ApiErrorResponse) {
        super(body.message)
        this.status = body.status
        this.code = body.code ?? 'unknown'
        this.fieldErrors = Array.isArray(body.fieldErrors)
            ? body.fieldErrors
            : Object.entries(body.fieldErrors ?? {}).map(([field, message]) => ({field, message}))
    }
}

function csrfHeaders(): Record<string, string> {
    const token = getToken()
    return token ? {'X-CSRF-TOKEN': token} : {}
}

function revisionHeaders(revision?: number): Record<string, string> {
    return revision === undefined ? {} : {'If-Match': `"${revision}"`}
}

function isRequestOptions(
    input?: ApiQueryParams | ApiRequestOptions,
): input is ApiRequestOptions {
    return input !== undefined && ('params' in input || 'revision' in input)
}

function normalizeQueryOptions(
    input?: ApiQueryParams | ApiRequestOptions,
): ApiRequestOptions {
    if (!input) {
        return {}
    }
    return isRequestOptions(input) ? input : {params: input}
}

function normalizeMutationOptions(input?: number | ApiRequestOptions): ApiRequestOptions {
    if (typeof input === 'number') {
        return {revision: input}
    }
    return input ?? {}
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
    async get<T>(path: string, paramsOrOptions?: ApiQueryParams | ApiRequestOptions): Promise<T> {
        const {params} = normalizeQueryOptions(paramsOrOptions)
        const url = new URL(path, window.location.origin)
        if (params) {
            Object.entries(params).forEach(([key, value]) => {
                if (value !== undefined) url.searchParams.append(key, String(value))
            })
        }
        const response = await fetch(url.toString(), {
            method: 'GET',
            credentials: 'same-origin',
            headers: {Accept: 'application/json', ...csrfHeaders()},
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

    async put<T>(path: string, body: unknown, revisionOrOptions?: number | ApiRequestOptions): Promise<T> {
        const {revision} = normalizeMutationOptions(revisionOrOptions)
        const response = await fetch(path, {
            method: 'PUT',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json',
                ...csrfHeaders(),
                ...revisionHeaders(revision),
            },
            body: JSON.stringify(body),
        })
        return handleResponse<T>(response)
    },

    async delete(path: string, revisionOrOptions?: number | ApiRequestOptions): Promise<void> {
        const {revision} = normalizeMutationOptions(revisionOrOptions)
        const response = await fetch(path, {
            method: 'DELETE',
            credentials: 'same-origin',
            headers: {Accept: 'application/json', ...csrfHeaders(), ...revisionHeaders(revision)},
        })
        await handleResponse<void>(response)
    },
}
