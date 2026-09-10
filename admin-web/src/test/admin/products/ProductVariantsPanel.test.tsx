import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {AdminContext, HttpError, RecordContextProvider} from 'react-admin'
import type {Product, ProductVariant} from '../../../api/types'
import {ProductVariantsPanel, VariantRow} from '../../../admin/products/ProductVariantsPanel'

const initial: ProductVariant = {
  id: 12,
  productId: 9,
  sku: 'BLUE',
  price: 20,
  stockQuantity: 10,
  imageUrl: null,
  active: true,
  defaultVariant: false,
  productRevision: 4,
}

const family: Product = {
  id: 9,
  sku: 'BASE',
  name: 'Router',
  description: null,
  price: 20,
  stockQuantity: 10,
  imageUrl: null,
  active: true,
  revision: 4,
  categories: [],
}

function setup() {
  const update = vi.fn().mockResolvedValue({data: {...initial, productRevision: 5}})
  const remove = vi.fn().mockResolvedValue({data: initial})
  const provider = {update, delete: remove}
  const tree = (variant: ProductVariant) => (
    <AdminContext dataProvider={provider}>
      <table>
        <tbody>
          <VariantRow variant={variant} />
        </tbody>
      </table>
    </AdminContext>
  )
  const view = render(tree(initial))
  return {update, remove, refetch: (variant: ProductVariant) => view.rerender(tree(variant))}
}

const stock = () =>
  screen.getByRole('spinbutton', {name: 'Stock for BLUE'}) as HTMLInputElement

it('locks variant creation until a pending request settles', async () => {
  let settle!: (value: {data: ProductVariant}) => void
  const create = vi.fn().mockReturnValue(
    new Promise<{data: ProductVariant}>((resolve) => {
      settle = resolve
    }),
  )

  render(
    <AdminContext dataProvider={{create, getList: vi.fn().mockResolvedValue({data: [], total: 0})}}>
      <RecordContextProvider value={family}>
        <ProductVariantsPanel />
      </RecordContextProvider>
    </AdminContext>,
  )

  fireEvent.change(screen.getByRole('textbox', {name: 'SKU'}), {target: {value: 'PURPLE'}})
  fireEvent.change(screen.getByRole('spinbutton', {name: 'Price'}), {target: {value: '19.99'}})
  fireEvent.change(screen.getByRole('spinbutton', {name: 'Stock'}), {target: {value: '2'}})
  fireEvent.click(screen.getByRole('button', {name: 'Add variant'}))

  await waitFor(() => expect(create).toHaveBeenCalledTimes(1))
  expect(screen.getByRole('group', {name: 'Add a variant'}).getAttribute('aria-busy')).toBe('true')
  expect((screen.getByRole('textbox', {name: 'SKU'}) as HTMLInputElement).disabled).toBe(true)
  expect((screen.getByRole('spinbutton', {name: 'Price'}) as HTMLInputElement).disabled).toBe(true)
  expect((screen.getByRole('spinbutton', {name: 'Stock'}) as HTMLInputElement).disabled).toBe(true)
  expect((screen.getByRole('button', {name: 'Add variant'}) as HTMLButtonElement).disabled).toBe(true)

  fireEvent.click(screen.getByRole('button', {name: 'Add variant'}))
  expect(create).toHaveBeenCalledTimes(1)

  settle({data: {...initial, sku: 'PURPLE'}})
  await waitFor(() =>
    expect((screen.getByRole('button', {name: 'Add variant'}) as HTMLButtonElement).disabled).toBe(false),
  )
})

it('refreshes pristine stock from server props', () => {
  const {refetch} = setup()

  refetch({...initial, stockQuantity: 5, productRevision: 5})

  expect(stock().value).toBe('5')
})

it('preserves dirty values until explicit discard after a refetch', async () => {
  const {update, refetch} = setup()
  fireEvent.change(stock(), {target: {value: '8'}})
  expect((screen.getByRole('button', {name: 'Remove'}) as HTMLButtonElement).disabled).toBe(true)

  refetch({...initial, stockQuantity: 5, productRevision: 5})

  expect(stock().value).toBe('8')
  expect(screen.getByRole('alert').textContent).toContain('changed')
  fireEvent.click(screen.getByRole('button', {name: 'Save'}))
  expect(update).not.toHaveBeenCalled()

  fireEvent.click(screen.getByRole('button', {name: 'Discard draft and reload'}))

  expect(stock().value).toBe('5')
  expect((screen.getByRole('button', {name: 'Remove'}) as HTMLButtonElement).disabled).toBe(false)
  fireEvent.change(stock(), {target: {value: '6'}})
  fireEvent.click(screen.getByRole('button', {name: 'Save'}))
  await waitFor(() =>
    expect(update).toHaveBeenCalledWith(
      'productVariants',
      expect.objectContaining({
        data: expect.objectContaining({stockQuantity: 6, revision: 5}),
        previousData: expect.objectContaining({stockQuantity: 6, productRevision: 5}),
      }),
    ),
  )
})

it('renders status from the active draft', () => {
  setup()

  fireEvent.click(screen.getByRole('checkbox', {name: 'Active for BLUE'}))

  expect(screen.getByText('Inactive')).toBeTruthy()
})

it('preserves a dirty sibling when another save advances the family revision', async () => {
  const sibling: ProductVariant = {...initial, id: 13, sku: 'RED', stockQuantity: 3}
  const update = vi.fn().mockResolvedValue({data: {...initial, stockQuantity: 11, productRevision: 5}})
  const provider = {update}
  const tree = (variants: ProductVariant[]) => (
    <AdminContext dataProvider={provider}>
      <table>
        <tbody>
          {variants.map((variant) => <VariantRow key={variant.id} variant={variant} />)}
        </tbody>
      </table>
    </AdminContext>
  )
  const view = render(tree([initial, sibling]))
  const blueStock = screen.getByRole('spinbutton', {name: 'Stock for BLUE'})
  const redStock = screen.getByRole('spinbutton', {name: 'Stock for RED'}) as HTMLInputElement
  fireEvent.change(redStock, {target: {value: '7'}})
  fireEvent.change(blueStock, {target: {value: '11'}})
  fireEvent.click(screen.getAllByRole('button', {name: 'Save'})[0])
  await waitFor(() => expect(update).toHaveBeenCalled())

  view.rerender(tree([
    {...initial, stockQuantity: 11, productRevision: 5},
    {...sibling, productRevision: 5},
  ]))

  expect(redStock.value).toBe('7')
  expect(screen.getByRole('alert').textContent).toContain('changed')
  expect((screen.getAllByRole('button', {name: 'Save'})[1] as HTMLButtonElement).disabled).toBe(true)
})

it('submits the original revision and preserves input on a 409', async () => {
  const {update} = setup()
  update.mockRejectedValue(new HttpError('Product changed', 409))
  fireEvent.change(stock(), {target: {value: '8'}})

  fireEvent.click(screen.getByRole('button', {name: 'Save'}))

  await waitFor(() =>
    expect(update).toHaveBeenCalledWith(
      'productVariants',
      expect.objectContaining({data: expect.objectContaining({stockQuantity: 8, revision: 4})}),
    ),
  )
  await screen.findByText('Product changed. Your draft is preserved; reload before retrying.')
  expect(stock().value).toBe('8')
})

it('edits images through the variant mutation', async () => {
  const {update} = setup()

  fireEvent.change(screen.getByRole('textbox', {name: 'Image URL for BLUE'}), {
    target: {value: '/images/blue.png'},
  })
  fireEvent.click(screen.getByRole('button', {name: 'Save'}))

  await waitFor(() =>
    expect(update).toHaveBeenCalledWith(
      'productVariants',
      expect.objectContaining({
        data: expect.objectContaining({imageUrl: '/images/blue.png', revision: 4}),
      }),
    ),
  )
})

it('blocks typing and removal while save is pending', async () => {
  const {update} = setup()
  let finish!: (value: {data: ProductVariant}) => void
  update.mockReturnValue(
    new Promise<{data: ProductVariant}>((resolve) => {
      finish = resolve
    }),
  )
  fireEvent.change(stock(), {target: {value: '8'}})

  fireEvent.click(screen.getByRole('button', {name: 'Save'}))

  await waitFor(() => expect(stock().disabled).toBe(true))
  expect((screen.getByRole('textbox', {name: 'SKU for BLUE'}) as HTMLInputElement).disabled).toBe(true)
  expect((screen.getByRole('spinbutton', {name: 'Price for BLUE'}) as HTMLInputElement).disabled).toBe(true)
  expect((screen.getByRole('checkbox', {name: 'Active for BLUE'}) as HTMLInputElement).disabled).toBe(true)
  expect((screen.getByRole('button', {name: 'Save'}) as HTMLButtonElement).disabled).toBe(true)
  expect((screen.getByRole('button', {name: 'Remove'}) as HTMLButtonElement).disabled).toBe(true)
  expect((screen.getByRole('button', {name: 'Discard draft and reload'}) as HTMLButtonElement).disabled).toBe(true)
  finish({data: {...initial, stockQuantity: 8, productRevision: 5}})
  await waitFor(() => expect(stock().disabled).toBe(false))
})

it('locks the row while removal is pending', async () => {
  const {remove} = setup()
  let finish!: (value: {data: ProductVariant}) => void
  remove.mockReturnValue(
    new Promise<{data: ProductVariant}>((resolve) => {
      finish = resolve
    }),
  )

  fireEvent.click(screen.getByRole('button', {name: 'Remove'}))

  await waitFor(() => expect(stock().disabled).toBe(true))
  expect((screen.getByRole('button', {name: 'Save'}) as HTMLButtonElement).disabled).toBe(true)
  expect((screen.getByRole('button', {name: 'Remove'}) as HTMLButtonElement).disabled).toBe(true)
  finish({data: initial})
  await waitFor(() => expect(stock().disabled).toBe(false))
})
