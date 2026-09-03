import {render, screen} from '@testing-library/react'
import {AdminContext, ResourceContextProvider} from 'react-admin'
import {ProductList} from './ProductList'

describe('Product resource', () => {
  it('renders server-backed product columns and inactive activation action', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    const consoleWarn = vi.spyOn(console, 'warn').mockImplementation(() => {})

    render(
      <AdminContext
        dataProvider={{
          getList: vi.fn().mockResolvedValue({
            data: [
              {
                id: 9,
                sku: 'SKU-9',
                name: 'Router',
                description: null,
                price: 199.99,
                stockQuantity: 8,
                imageUrl: null,
                active: false,
                revision: 4,
                categories: [],
                categoryIds: [],
              },
            ],
            total: 1,
          }),
        }}
      >
        <ResourceContextProvider value="products">
          <ProductList />
        </ResourceContextProvider>
      </AdminContext>,
    )

    expect(await screen.findByText('Router')).toBeTruthy()
    expect(screen.getByText('SKU-9')).toBeTruthy()
    expect(screen.getByRole('button', {name: /activate router/i})).toBeTruthy()
    expect(consoleError).not.toHaveBeenCalled()
    expect(consoleWarn).not.toHaveBeenCalled()

    consoleError.mockRestore()
    consoleWarn.mockRestore()
  })
})
