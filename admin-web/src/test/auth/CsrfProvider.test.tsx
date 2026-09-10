import {act, render, screen} from '@testing-library/react'
import {StrictMode, useLayoutEffect} from 'react'
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import {clearToken, CsrfProvider, getCsrf, refreshToken, setToken, useCsrf} from '../../auth/CsrfProvider'

function CsrfState() {
  const {headerName, token} = useCsrf()
  return <output>{headerName && token ? `${headerName}:${token}` : 'missing'}</output>
}

function PublishCsrfInLayoutEffect() {
  useLayoutEffect(() => {
    setToken('layout-token', 'X-LAYOUT-CSRF')
  }, [])
  return null
}

describe('CsrfProvider', () => {
  beforeEach(() => {
    clearToken()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('bootstraps the server-provided header name and token on mount', async () => {
    let resolveResponse: (response: Response) => void = () => {}
    vi.stubGlobal('fetch', vi.fn().mockReturnValue(new Promise<Response>((resolve) => {
      resolveResponse = resolve
    })))
    const response = new Response(JSON.stringify({
      headerName: 'X-XSRF-TOKEN',
      token: 'csrf-bootstrap',
    }), {
      status: 200,
      headers: {'Content-Type': 'application/json'},
    })

    render(
      <StrictMode>
        <CsrfProvider>
          <CsrfState />
        </CsrfProvider>
      </StrictMode>,
    )
    await act(async () => {
      resolveResponse(response)
    })

    expect(screen.getByText('X-XSRF-TOKEN:csrf-bootstrap')).toBeTruthy()
    expect(fetch).toHaveBeenCalledWith('/api/admin/csrf', {
      credentials: 'same-origin',
      headers: {Accept: 'application/json'},
    })
    expect(fetch).toHaveBeenCalledTimes(1)
  })

  it('observes a token published between render and the passive subscription effect', () => {
    render(
      <CsrfProvider>
        <PublishCsrfInLayoutEffect />
        <CsrfState />
      </CsrfProvider>,
    )

    expect(screen.getByText('X-LAYOUT-CSRF:layout-token')).toBeTruthy()
  })

  it('can refresh again after a failed request', async () => {
    vi.stubGlobal('fetch', vi.fn()
      .mockRejectedValueOnce(new Error('network unavailable'))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        headerName: 'X-RECOVERED-CSRF',
        token: 'recovered-token',
      }), {
        status: 200,
        headers: {'Content-Type': 'application/json'},
      })))

    await refreshToken()
    expect(getCsrf()).toBeNull()

    await refreshToken()
    expect(getCsrf()).toEqual({
      headerName: 'X-RECOVERED-CSRF',
      token: 'recovered-token',
    })
  })
})
