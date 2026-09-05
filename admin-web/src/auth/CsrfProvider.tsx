import { createContext, useContext } from 'react'
import type { ReactNode } from 'react'

let _token: string | null = null
const _listeners = new Set<(token: string | null) => void>()

export function getToken(): string | null {
    return _token
}

export function setToken(token: string | null) {
    _token = token
    _listeners.forEach(listener => listener(token))
}

export function clearToken() {
    setToken(null)
}

export async function refreshToken(): Promise<void> {
    try {
        const response = await fetch('/api/admin/csrf', {
            credentials: 'same-origin',
            headers: { Accept: 'application/json' },
        })
        if (response.ok) {
            const data = (await response.json()) as { token: string }
            setToken(data.token)
        }
    } catch {
        clearToken()
    }
}

interface CsrfContextValue {
    token: string | null
    refresh: () => Promise<void>
}

const CsrfContext = createContext<CsrfContextValue>({
    token: null,
    refresh: async () => {},
})

export function CsrfProvider({ children }: { children: ReactNode }) {
    const token = getToken()

    const refresh = async () => {
        await refreshToken()
    }

    return (
        <CsrfContext.Provider value={{ token, refresh }}>
            {children}
        </CsrfContext.Provider>
    )
}

export function useCsrf() {
    return useContext(CsrfContext)
}
