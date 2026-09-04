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

            const { ApiClient } = await import('../../api/client')
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

            const { ApiClient } = await import('../../api/client')
            const csrfModule = await import('../../auth/CsrfProvider')
            csrfModule.setToken(csrfToken)

            await ApiClient.post('/api/admin/products', { name: 'Test' })

            expect((global.fetch as any).mock.calls[0][1].headers['X-CSRF-TOKEN']).toBe(csrfToken)
            csrfModule.clearToken()
        })

    it('delete includes CSRF header', async () => {
            const csrfToken = 'csrf-456'
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: true,
                status: 204,
                headers: new Headers(),
            })

            const { ApiClient } = await import('../../api/client')
            const csrfModule = await import('../../auth/CsrfProvider')
            csrfModule.setToken(csrfToken)

            await ApiClient.delete('/api/admin/products/1')

            expect((global.fetch as any).mock.calls[0][1].headers['X-CSRF-TOKEN']).toBe(csrfToken)
            csrfModule.clearToken()
        })

    it('get accepts query params through typed request options', async () => {
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: true,
                status: 200,
                headers: new Headers({ 'content-type': 'application/json' }),
                json: async () => ({ content: [] }),
            })

            const { ApiClient } = await import('../../api/client')
            await ApiClient.get('/api/admin/products', {
                params: { page: 1, size: 20, q: 'router', active: false },
            })

            expect((global.fetch as any).mock.calls[0][0]).toBe(
                'http://localhost:3000/api/admin/products?page=1&size=20&q=router&active=false',
            )
        })

    it('put accepts revision through typed request options', async () => {
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: true,
                status: 200,
                headers: new Headers({ 'content-type': 'application/json' }),
                json: async () => ({ id: 1 }),
            })

            const { ApiClient } = await import('../../api/client')
            await ApiClient.put('/api/admin/products/1', { name: 'Updated' }, { revision: 4 })

            expect((global.fetch as any).mock.calls[0][1].headers['If-Match']).toBe('"4"')
        })

    it('non-2xx responses throw typed ApiErrorResponse', async () => {
            const errorBody = { message: 'Not found', status: 404 }
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: false,
                status: 404,
                headers: new Headers({ 'content-type': 'application/json' }),
                json: async () => errorBody,
            })

            const { ApiClient } = await import('../../api/client')
            await expect(ApiClient.get('/api/admin/products')).rejects.toMatchObject({
                status: 404,
                message: 'Not found',
            })
        })
    })
