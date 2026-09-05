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
})
