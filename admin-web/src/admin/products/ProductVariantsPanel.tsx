import {HttpError, useCreate, useDelete, useGetList, useNotify, useRecordContext, useRefresh, useUpdate} from 'react-admin'
import {type FormEvent, useState} from 'react'
import type {Product, ProductVariant} from '../../api/types'

type VariantForm = Pick<ProductVariant, 'sku' | 'price' | 'stockQuantity' | 'imageUrl' | 'active'>
type ProductVariantMutationRecord = ProductVariant & {revision?: number}

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
  const [create, {isPending: creating}] = useCreate()
  const [form, setForm] = useState<VariantForm>(blankVariant)

  if (!product) return null

  function submit(event: FormEvent) {
    event.preventDefault()
    if (creating || productId === undefined) return
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
          <thead><tr><th>SKU</th><th>Price</th><th>Stock</th><th>Image URL</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>{data.map((variant) => <VariantRow key={variant.id} variant={variant} />)}</tbody>
        </table>
      </div>}
      <form className="mt-4" onSubmit={submit}>
        <fieldset className="grid gap-3 md:grid-cols-5" disabled={creating} aria-busy={creating}>
          <legend className="sr-only">Add a variant</legend>
          <label className="form-control" htmlFor="new-variant-sku"><span className="label-text">SKU</span><input id="new-variant-sku" name="newVariant.sku" className="input input-bordered" disabled={creating} value={form.sku} onChange={(event) => setForm({...form, sku: event.target.value})} required /></label>
          <label className="form-control" htmlFor="new-variant-price"><span className="label-text">Price</span><input id="new-variant-price" name="newVariant.price" className="input input-bordered" disabled={creating} type="number" min="0.01" step="0.01" value={form.price} onChange={(event) => setForm({...form, price: Number(event.target.value)})} required /></label>
          <label className="form-control" htmlFor="new-variant-stockQuantity"><span className="label-text">Stock</span><input id="new-variant-stockQuantity" name="newVariant.stockQuantity" className="input input-bordered" disabled={creating} type="number" min="0" value={form.stockQuantity} onChange={(event) => setForm({...form, stockQuantity: Number(event.target.value)})} required /></label>
          <label className="form-control" htmlFor="new-variant-imageUrl"><span className="label-text">Image URL</span><input id="new-variant-imageUrl" name="newVariant.imageUrl" className="input input-bordered" disabled={creating} value={form.imageUrl ?? ''} onChange={(event) => setForm({...form, imageUrl: event.target.value || null})} /></label>
          <button className="btn btn-primary self-end" disabled={creating} type="submit">Add variant</button>
        </fieldset>
      </form>
    </section>
  )
}

export function VariantRow({variant}: {variant: ProductVariant}) {
  const notify = useNotify()
  const refresh = useRefresh()
  const [update, {isPending: updating}] = useUpdate<ProductVariantMutationRecord>()
  const [remove, {isPending: removing}] = useDelete()
  const [draft, setDraft] = useState<ProductVariant | null>(null)
  const [failure, setFailure] = useState<string | null>(null)
  const form = draft ?? variant
  const changed = draft !== null && draft.productRevision !== variant.productRevision
  const busy = updating || removing
  const fieldId = (field: string) => `variant-${variant.id}-${field}`
  const fieldName = (field: string) => `variants[${variant.id}].${field}`

  function save() {
    if (busy || changed || failure !== null) return
    update(
      'productVariants',
      {id: variant.id, data: {...form, revision: form.productRevision}, previousData: form},
      {
        mutationMode: 'pessimistic',
        onSuccess: () => {
          setDraft(null)
          setFailure(null)
          notify('Variant updated', {type: 'success'})
          refresh()
        },
        onError: (error) => {
          if (error instanceof HttpError && error.status === 409) {
            setFailure('Product changed. Your draft is preserved; reload before retrying.')
            refresh()
            return
          }
          notify('Unable to update variant', {type: 'error'})
        },
      },
    )
  }

  function removeVariant() {
    if (busy || draft !== null) return
    remove(
      'productVariants',
      {id: variant.id, previousData: variant},
      {
        mutationMode: 'pessimistic',
        onSuccess: () => {
          notify('Variant removed', {type: 'success'})
          refresh()
        },
        onError: () => notify('Unable to remove variant', {type: 'error'}),
      },
    )
  }

  return <tr>
    <td><input id={fieldId('sku')} name={fieldName('sku')} className="input input-sm input-bordered" disabled={busy} value={form.sku} onChange={(event) => setDraft({...form, sku: event.target.value})} aria-label={`SKU for ${variant.sku}`} /></td>
    <td><input id={fieldId('price')} name={fieldName('price')} className="input input-sm input-bordered w-28" disabled={busy} type="number" min="0.01" step="0.01" value={form.price} onChange={(event) => setDraft({...form, price: Number(event.target.value)})} aria-label={`Price for ${variant.sku}`} /></td>
    <td><input id={fieldId('stockQuantity')} name={fieldName('stockQuantity')} className="input input-sm input-bordered w-24" disabled={busy} type="number" min="0" value={form.stockQuantity} onChange={(event) => setDraft({...form, stockQuantity: Number(event.target.value)})} aria-label={`Stock for ${variant.sku}`} /></td>
    <td><input id={fieldId('imageUrl')} name={fieldName('imageUrl')} className="input input-sm input-bordered" disabled={busy} value={form.imageUrl ?? ''} onChange={(event) => setDraft({...form, imageUrl: event.target.value || null})} aria-label={`Image URL for ${variant.sku}`} /></td>
    <td><label className="label cursor-pointer gap-2" htmlFor={fieldId('active')}><span>{form.active ? 'Active' : 'Inactive'}</span><input id={fieldId('active')} name={fieldName('active')} className="toggle toggle-primary" disabled={busy} type="checkbox" checked={form.active} onChange={(event) => setDraft({...form, active: event.target.checked})} aria-label={`Active for ${variant.sku}`} /></label></td>
    <td>
      <div className="flex flex-wrap gap-2">
        <button className="btn btn-sm btn-primary" type="button" disabled={busy || changed || failure !== null} onClick={save}>Save</button>
        <button className="btn btn-sm btn-error" type="button" disabled={variant.defaultVariant || busy || draft !== null} onClick={removeVariant}>{variant.defaultVariant ? 'Default' : 'Remove'}</button>
        {draft !== null || failure !== null ? <button className="btn btn-sm" type="button" disabled={busy} onClick={() => { setDraft(null); setFailure(null); refresh() }}>Discard draft and reload</button> : null}
      </div>
      {changed || failure !== null ? <p className="alert alert-warning mt-2 py-2 text-sm" role="alert">{failure ?? 'Product changed on the server. Discard this draft before editing again.'}</p> : null}
    </td>
  </tr>
}
