import { describe, it, expect, vi, beforeEach } from 'vitest'

describe('ApiClient', () => {
    beforeEach(() => {
        vi.resetModules()
        global.fetch = vi.fn()
    })

    it('get returns parsed JSON', async () => {
        const data = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 }
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: true,
                status: 200,
                headers: new Headers({ 'content-type': 'application/json' }),
                json: async () => data,
            })

            const { ApiClient } = await import('./client')
            const result = await ApiClient.get('/api/admin/products')
            expect(result).toEqual(data)
        })

    it('post includes CSRF header from CsrfProvider', async () => {
            const csrfToken = 'csrf-123'
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: true,
                status: 200,
                headers: new Headers({ 'content-type': 'application/json' }),
                json: async () => ({ success: true }),
            })

            const { ApiClient } = await import('./client')
            const csrfModule = await import('../auth/CsrfProvider')
            csrfModule.setToken(csrfToken)

            await ApiClient.post('/api/admin/products', { name: 'Test' })

            expect((global.fetch as any).mock.calls[0][1].headers['X-XSRF-TOKEN']).toBe(csrfToken)
            csrfModule.clearToken()
        })

    it('delete includes CSRF header', async () => {
            const csrfToken = 'csrf-456'
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: true,
                status: 204,
                headers: new Headers(),
            })

            const { ApiClient } = await import('./client')
            const csrfModule = await import('../auth/CsrfProvider')
            csrfModule.setToken(csrfToken)

            await ApiClient.delete('/api/admin/products/1')

            expect((global.fetch as any).mock.calls[0][1].headers['X-XSRF-TOKEN']).toBe(csrfToken)
            csrfModule.clearToken()
        })

    it('non-2xx responses throw typed ApiErrorResponse', async () => {
            const errorBody = { message: 'Not found', status: 404 }
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: false,
                status: 404,
                headers: new Headers({ 'content-type': 'application/json' }),
                json: async () => errorBody,
            })

            const { ApiClient } = await import('./client')
            await expect(ApiClient.get('/api/admin/products')).rejects.toMatchObject({
                status: 404,
                message: 'Not found',
            })
        })
    })
