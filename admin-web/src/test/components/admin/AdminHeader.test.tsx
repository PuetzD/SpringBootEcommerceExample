import {beforeEach, describe, expect, it, vi} from 'vitest'
import {clearToken, setToken} from '../../../auth/CsrfProvider'
import {logout} from '../../../components/admin/AdminHeader'

describe('AdminHeader logout', () => {
  beforeEach(() => {
    clearToken()
    vi.stubGlobal('fetch', vi.fn())
  })

  it('does not submit logout when CSRF refresh does not provide a token', async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({}), {
        status: 200,
        headers: {'Content-Type': 'application/json'},
      }),
    )

    await expect(logout()).rejects.toThrow('Unable to obtain CSRF token')

    expect(fetch).toHaveBeenCalledTimes(1)
    expect(fetch).toHaveBeenCalledWith('/api/admin/csrf', expect.anything())
    setToken(null)
  })

  it('submits logout with the CSRF header name supplied by the server', async () => {
    setToken('logout-token', 'X-XSRF-TOKEN')
    vi.mocked(fetch).mockRejectedValueOnce(new Error('stop after request capture'))

    await expect(logout()).rejects.toThrow('stop after request capture')

    expect(fetch).toHaveBeenCalledWith('/admin/logout', {
      method: 'POST',
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
        'X-XSRF-TOKEN': 'logout-token',
      },
      body: undefined,
    })
  })
})
