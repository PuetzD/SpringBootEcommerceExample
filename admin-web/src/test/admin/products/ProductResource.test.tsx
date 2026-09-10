import {fireEvent, render, screen, waitFor, within} from '@testing-library/react'
import {AdminContext, ResourceContextProvider} from 'react-admin'
import {ProductCreate} from '../../../admin/products/ProductCreate'
import {ProductList} from '../../../admin/products/ProductList'

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
    for (const label of ['sku', 'name', 'price', 'stockQuantity', 'active', 'categories']) {
      const header = screen.getByRole('columnheader', {name: new RegExp(label, 'i')})
      expect(within(header).queryByRole('button')).toBeNull()
    }
    expect(consoleError).not.toHaveBeenCalled()
    expect(consoleWarn).not.toHaveBeenCalled()

    consoleError.mockRestore()
    consoleWarn.mockRestore()
  })

  it('requires confirmation before activating an inactive product', async () => {
    const update = vi.fn().mockResolvedValue({data: {}})

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
          update,
        }}
      >
        <ResourceContextProvider value="products">
          <ProductList />
        </ResourceContextProvider>
      </AdminContext>,
    )

    fireEvent.click(await screen.findByRole('button', {name: /activate router/i}))

    expect(screen.getByRole('dialog', {name: /activate router/i})).toBeTruthy()
    expect(update).not.toHaveBeenCalled()

    fireEvent.click(screen.getByRole('button', {name: /confirm/i}))

    await waitFor(() =>
      expect(update).toHaveBeenCalledWith(
        'products',
        expect.objectContaining({
          data: expect.objectContaining({active: true}),
          id: 9,
        }),
      ),
    )
  })

  it('announces client-required Product create fields before submitting', async () => {
    const create = vi.fn().mockResolvedValue({data: {id: 9}})

    render(
      <AdminContext
        dataProvider={{
          create,
          getList: vi.fn().mockResolvedValue({data: [], total: 0}),
        }}
      >
        <ResourceContextProvider value="products">
          <ProductCreate />
        </ResourceContextProvider>
      </AdminContext>,
    )

    fireEvent.change(screen.getByRole('textbox', {name: /sku/i}), {target: {value: ''}})
    fireEvent.change(screen.getByRole('textbox', {name: /name/i}), {target: {value: ''}})
    fireEvent.change(screen.getByRole('spinbutton', {name: /price/i}), {target: {value: ''}})
    fireEvent.change(screen.getByRole('spinbutton', {name: /stockquantity/i}), {target: {value: ''}})
    const form = (await screen.findByRole('button', {name: /save/i})).closest('form')
    if (!form) throw new Error('Product create form is missing')
    fireEvent.submit(form)

    await waitFor(() => expect(screen.getAllByText('Required field')).toHaveLength(4))
    expect(create).not.toHaveBeenCalled()
  })
})
