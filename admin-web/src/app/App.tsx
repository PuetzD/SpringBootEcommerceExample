import { RouterProvider } from 'react-router-dom'
import { CsrfProvider } from '../auth/CsrfProvider'
import { router } from './router'

export function App() {
  return (
    <CsrfProvider>
      <RouterProvider router={router} />
    </CsrfProvider>
  )
}
