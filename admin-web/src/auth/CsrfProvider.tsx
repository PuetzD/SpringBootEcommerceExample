import {createContext, useContext, useEffect, useState} from 'react'
import type { ReactNode } from 'react'

export type CsrfState = {
    headerName: string
    token: string
}

let _csrf: CsrfState | null = null
let _refreshPromise: Promise<void> | null = null
const _listeners = new Set<(csrf: CsrfState | null) => void>()

export function getCsrf(): CsrfState | null {
    return _csrf
}

export function setToken(token: string | null, headerName = 'X-CSRF-TOKEN') {
    _csrf = token ? {headerName, token} : null
    _listeners.forEach(listener => listener(_csrf))
}

export function clearToken() {
    setToken(null)
}

export async function refreshToken(): Promise<void> {
    if (!_refreshPromise) {
        _refreshPromise = (async () => {
            try {
                const response = await fetch('/api/admin/csrf', {
                    credentials: 'same-origin',
                    headers: {Accept: 'application/json'},
                })
                if (!response.ok) {
                    clearToken()
                    return
                }
                const data = (await response.json()) as Partial<CsrfState>
                if (typeof data.headerName === 'string' && typeof data.token === 'string') {
                    setToken(data.token, data.headerName)
                } else {
                    clearToken()
                }
            } catch {
                clearToken()
            }
        })().finally(() => {
            _refreshPromise = null
        })
    }
    await _refreshPromise
}

interface CsrfContextValue {
    headerName: string | null
    token: string | null
    refresh: () => Promise<void>
}

const CsrfContext = createContext<CsrfContextValue>({
    headerName: null,
    token: null,
    refresh: refreshToken,
})

export function CsrfProvider({children}: {children: ReactNode}) {
    const [csrf, setCsrf] = useState<CsrfState | null>(() => getCsrf())

    useEffect(() => {
        _listeners.add(setCsrf)
        if (!getCsrf()) {
            void refreshToken()
        }
        return () => {
            _listeners.delete(setCsrf)
        }
    }, [])

    return (
        <CsrfContext.Provider value={{
            headerName: csrf?.headerName ?? null,
            token: csrf?.token ?? null,
            refresh: refreshToken,
        }}>
            {children}
        </CsrfContext.Provider>
    )
}

export function useCsrf() {
    return useContext(CsrfContext)
}
