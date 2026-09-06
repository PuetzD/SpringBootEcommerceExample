import {useCreate, useDelete, useGetList, useNotify, useRecordContext, useRefresh, useUpdate} from 'react-admin'
import {type FormEvent, useState} from 'react'
import type {Product, ProductVariant} from '../../api/types'

type VariantForm = Pick<ProductVariant, 'sku' | 'price' | 'stockQuantity' | 'imageUrl' | 'active'>

const blankVariant: VariantForm = {
  sku: '',
  price: 0,
  stockQuantity: 0,
  imageUrl: null,
  active: true,
}

export function ProductVariantsPanel() {
  const product = useRecordContext<Product>()
  const productId = product?.id
  const notify = useNotify()
  const refresh = useRefresh()
  const {data = [], isPending} = useGetList<ProductVariant>('productVariants', {
    filter: {productId: product?.id},
    pagination: {page: 1, perPage: 100},
  })
  const [create] = useCreate()
  const [form, setForm] = useState<VariantForm>(blankVariant)

  if (!product) return null

  function submit(event: FormEvent) {
    event.preventDefault()
    if (productId === undefined) return
    create(
      'productVariants',
      {data: {...form, productId, revision: product?.revision}},
      {
        onSuccess: () => {
          notify('Variant created', {type: 'success'})
          setForm(blankVariant)
          refresh()
        },
        onError: () => notify('Unable to create variant', {type: 'error'}),
      },
    )
  }

  return (
    <section className="card bg-base-100 shadow-sm mt-4 p-4" aria-labelledby="variants-heading">
      <h2 id="variants-heading" className="card-title mb-3">Sellable variants</h2>
      {isPending ? <p>Loading variants…</p> : <div className="overflow-x-auto">
        <table className="table table-zebra">
          <caption className="sr-only">Product variants</caption>
          <thead><tr><th>SKU</th><th>Price</th><th>Stock</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>{data.map((variant) => <VariantRow key={variant.id} variant={variant} />)}</tbody>
        </table>
      </div>}
      <form className="mt-4 grid gap-3 md:grid-cols-5" onSubmit={submit}>
        <label className="form-control"><span className="label-text">SKU</span><input className="input input-bordered" value={form.sku} onChange={(event) => setForm({...form, sku: event.target.value})} required /></label>
        <label className="form-control"><span className="label-text">Price</span><input className="input input-bordered" type="number" min="0.01" step="0.01" value={form.price} onChange={(event) => setForm({...form, price: Number(event.target.value)})} required /></label>
        <label className="form-control"><span className="label-text">Stock</span><input className="input input-bordered" type="number" min="0" value={form.stockQuantity} onChange={(event) => setForm({...form, stockQuantity: Number(event.target.value)})} required /></label>
        <label className="form-control"><span className="label-text">Image URL</span><input className="input input-bordered" value={form.imageUrl ?? ''} onChange={(event) => setForm({...form, imageUrl: event.target.value || null})} /></label>
        <button className="btn btn-primary self-end" type="submit">Add variant</button>
      </form>
    </section>
  )
}

function VariantRow({variant}: {variant: ProductVariant}) {
  const notify = useNotify()
  const refresh = useRefresh()
  const [update, {isPending: updating}] = useUpdate()
  const [remove, {isPending: removing}] = useDelete()
  const [form, setForm] = useState<VariantForm>(variant)

  function save() {
    update(
      'productVariants',
      {id: variant.id, data: {...form, productId: variant.productId, revision: variant.productRevision}, previousData: variant},
      {onSuccess: () => { notify('Variant updated', {type: 'success'}); refresh() }, onError: () => notify('Unable to update variant', {type: 'error'})},
    )
  }

  function removeVariant() {
    remove(
      'productVariants',
      {id: variant.id, previousData: variant},
      {onSuccess: () => { notify('Variant removed', {type: 'success'}); refresh() }, onError: () => notify('Unable to remove variant', {type: 'error'})},
    )
  }

  return <tr>
    <td><input className="input input-sm input-bordered" value={form.sku} onChange={(event) => setForm({...form, sku: event.target.value})} aria-label={`SKU for ${variant.sku}`} /></td>
    <td><input className="input input-sm input-bordered w-28" type="number" min="0.01" step="0.01" value={form.price} onChange={(event) => setForm({...form, price: Number(event.target.value)})} aria-label={`Price for ${variant.sku}`} /></td>
    <td><input className="input input-sm input-bordered w-24" type="number" min="0" value={form.stockQuantity} onChange={(event) => setForm({...form, stockQuantity: Number(event.target.value)})} aria-label={`Stock for ${variant.sku}`} /></td>
    <td><label className="label cursor-pointer gap-2"><span>{variant.active ? 'Active' : 'Inactive'}</span><input className="toggle toggle-primary" type="checkbox" checked={form.active} onChange={(event) => setForm({...form, active: event.target.checked})} aria-label={`Active for ${variant.sku}`} /></label></td>
    <td className="flex gap-2"><button className="btn btn-sm btn-primary" type="button" disabled={updating} onClick={save}>Save</button><button className="btn btn-sm btn-error" type="button" disabled={variant.defaultVariant || removing} onClick={removeVariant}>{variant.defaultVariant ? 'Default' : 'Remove'}</button></td>
  </tr>
}
