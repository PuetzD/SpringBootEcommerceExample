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

    it('post uses the CSRF header name supplied by the server', async () => {
            const csrfToken = 'csrf-123'
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: true,
                status: 200,
                headers: new Headers({ 'content-type': 'application/json' }),
                json: async () => ({ success: true }),
            })

            const { ApiClient } = await import('../../api/client')
            const csrfModule = await import('../../auth/CsrfProvider')
            csrfModule.setToken(csrfToken, 'X-XSRF-TOKEN')

            await ApiClient.post('/api/admin/products', { name: 'Test' })

            expect((global.fetch as any).mock.calls[0][1].headers['X-XSRF-TOKEN']).toBe(csrfToken)
            expect((global.fetch as any).mock.calls[0][1].headers['X-CSRF-TOKEN']).toBeUndefined()
            csrfModule.clearToken()
        })

    it('waits for shared CSRF bootstrap before sending exactly one mutation', async () => {
            let resolveBootstrap!: (response: Response) => void
            const bootstrapResponse = new Promise<Response>((resolve) => {
                resolveBootstrap = resolve
            })
            ; (global.fetch as any)
                .mockReturnValueOnce(bootstrapResponse)
                .mockResolvedValueOnce(new Response(JSON.stringify({id: 9}), {
                    status: 200,
                    headers: {'Content-Type': 'application/json'},
                }))

            const {ApiClient} = await import('../../api/client')
            const csrfModule = await import('../../auth/CsrfProvider')
            const bootstrap = csrfModule.refreshToken()
            const mutation = ApiClient.post('/api/admin/products', {name: 'Test'})

            await Promise.resolve()
            expect(global.fetch).toHaveBeenCalledTimes(1)
            expect(global.fetch).toHaveBeenCalledWith('/api/admin/csrf', {
                credentials: 'same-origin',
                headers: {Accept: 'application/json'},
            })

            resolveBootstrap(new Response(JSON.stringify({
                headerName: 'X-BOOTSTRAP-CSRF',
                token: 'ready-token',
            }), {
                status: 200,
                headers: {'Content-Type': 'application/json'},
            }))
            await bootstrap
            await mutation

            expect(global.fetch).toHaveBeenCalledTimes(2)
            expect(global.fetch).toHaveBeenNthCalledWith(2, '/api/admin/products', {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json',
                    'X-BOOTSTRAP-CSRF': 'ready-token',
                },
                body: '{"name":"Test"}',
            })
        })

    it('rejects a mutation without sending it when CSRF bootstrap fails', async () => {
            ; (global.fetch as any).mockResolvedValueOnce(new Response(null, {status: 503}))

            const {ApiClient} = await import('../../api/client')

            await expect(ApiClient.post('/api/admin/products', {name: 'Test'})).rejects.toThrow(
                'Unable to obtain CSRF token',
            )

            expect(global.fetch).toHaveBeenCalledTimes(1)
            expect(global.fetch).toHaveBeenCalledWith('/api/admin/csrf', {
                credentials: 'same-origin',
                headers: {Accept: 'application/json'},
            })
        })

    it('delete includes the dynamic CSRF header, quoted revision, and accepts 204', async () => {
            const csrfToken = 'csrf-456'
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: true,
                status: 204,
                headers: new Headers(),
            })

            const { ApiClient } = await import('../../api/client')
            const csrfModule = await import('../../auth/CsrfProvider')
            csrfModule.setToken(csrfToken, 'X-CUSTOM-CSRF')

            await ApiClient.delete('/api/admin/products/1', {revision: 4})

            expect((global.fetch as any).mock.calls[0][1].headers['X-CUSTOM-CSRF']).toBe(csrfToken)
            expect((global.fetch as any).mock.calls[0][1].headers['If-Match']).toBe('"4"')
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
                params: { page: 0, size: 20, q: null, active: false },
            })

            expect((global.fetch as any).mock.calls[0][0]).toBe(
                'http://localhost:3000/api/admin/products?page=0&size=20&active=false',
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
            const csrfModule = await import('../../auth/CsrfProvider')
            csrfModule.setToken('csrf-put')
            await ApiClient.put('/api/admin/products/1', { name: 'Updated' }, { revision: 4 })

            expect((global.fetch as any).mock.calls[0][1].headers['If-Match']).toBe('"4"')
            csrfModule.clearToken()
        })

    it('patches with credentials, CSRF and expected revision', async () => {
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: true,
                status: 200,
                headers: new Headers({ 'content-type': 'application/json' }),
                json: async () => ({ id: 9 }),
            })

            const { ApiClient } = await import('../../api/client')
            const csrf = await import('../../auth/CsrfProvider')
            csrf.setToken('csrf-family')
            try {
                await ApiClient.patch('/api/admin/products/9/family', { name: 'Family' }, { revision: 4 })
                expect(global.fetch).toHaveBeenCalledWith('/api/admin/products/9/family', {
                    method: 'PATCH',
                    credentials: 'same-origin',
                    body: '{"name":"Family"}',
                    headers: {
                        'Content-Type': 'application/json',
                        Accept: 'application/json',
                        'X-CSRF-TOKEN': 'csrf-family',
                        'If-Match': '"4"',
                    },
                })
            } finally {
                csrf.clearToken()
            }
        })

    it('non-2xx responses throw typed ApiErrorResponse', async () => {
            const errorBody = {
                message: 'Name is required',
                status: 400,
                code: 'request.validation',
                fieldErrors: {name: 'Name is required'},
            }
            ; (global.fetch as any).mockResolvedValueOnce({
                ok: false,
                status: 400,
                headers: new Headers({ 'content-type': 'application/json' }),
                json: async () => errorBody,
            })

            const { ApiClient } = await import('../../api/client')
            await expect(ApiClient.get('/api/admin/products')).rejects.toMatchObject({
                status: 400,
                code: 'request.validation',
                message: 'Name is required',
                fieldErrors: [{field: 'name', message: 'Name is required'}],
            })
        })

    it('does not replay a forbidden mutation after refreshing CSRF', async () => {
            ; (global.fetch as any)
                .mockResolvedValueOnce({
                    ok: false,
                    status: 403,
                    headers: new Headers({'content-type': 'application/json'}),
                    json: async () => ({
                        message: 'Forbidden',
                        status: 403,
                        code: 'authorization.denied',
                        fieldErrors: {},
                    }),
                })
                .mockResolvedValueOnce(new Response(JSON.stringify({
                    headerName: 'X-CSRF-TOKEN',
                    token: 'replacement',
                }), {
                    status: 200,
                    headers: {'Content-Type': 'application/json'},
                }))

            const {ApiClient} = await import('../../api/client')
            const csrfModule = await import('../../auth/CsrfProvider')
            csrfModule.setToken('expired-token')
            await expect(ApiClient.post('/api/admin/products', {name: 'Test'})).rejects.toMatchObject({status: 403})

            const requestedUrls = vi.mocked(fetch).mock.calls.map(([url]) => String(url))
            expect(requestedUrls.filter((url) => url.includes('/api/admin/products'))).toHaveLength(1)
        })

    it.each([
            {status: 401, code: 'authentication.required', message: 'Unauthorized'},
            {status: 403, code: 'authorization.denied', message: 'Forbidden'},
        ])('preserves the original $status API error when CSRF refresh fails', async ({status, code, message}) => {
            ; (global.fetch as any)
                .mockResolvedValueOnce({
                    ok: false,
                    status,
                    headers: new Headers({'content-type': 'application/json'}),
                    json: async () => ({message, status, code, fieldErrors: {}}),
                })
                .mockResolvedValueOnce(new Response(null, {status: 503}))

            const {ApiClient} = await import('../../api/client')
            const csrfModule = await import('../../auth/CsrfProvider')
            csrfModule.setToken('expired-token')

            await expect(ApiClient.post('/api/admin/products', {name: 'Test'})).rejects.toMatchObject({
                status,
                code,
                message,
            })

            const requestedUrls = vi.mocked(fetch).mock.calls.map(([url]) => String(url))
            expect(requestedUrls.filter((url) => url.includes('/api/admin/products'))).toHaveLength(1)
            expect(requestedUrls.filter((url) => url.includes('/api/admin/csrf'))).toHaveLength(1)
        })
    })
