import { render, screen } from '@testing-library/react'
import { EmptyState } from '../../../components/admin/EmptyState'
import { LoadingState } from '../../../components/admin/LoadingState'

describe('state components', () => {
  it('shows meaningful loading and empty state text', () => {
    render(<LoadingState message="Loading products" />)
    render(<EmptyState title="No products" description="Add your first product to get started." />)

    expect(screen.getByText(/loading products/i)).toBeTruthy()
    expect(screen.getByText(/no products/i)).toBeTruthy()
    expect(screen.getByText(/add your first product to get started/i)).toBeTruthy()
  })
})
