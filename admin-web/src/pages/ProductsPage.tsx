import {useEffect, useRef, useState} from 'react'
import {ApiError} from '../api/client'
import {createProduct, deactivateProduct, getProduct, listCategoryOptions, listProducts, updateProduct} from '../api/catalog'
import type {CategoryOption, PageResponse, Product, UpdateProductInput} from '../api/types'
import {ConfirmDialog} from '../components/admin/ConfirmDialog'
import {EmptyState} from '../components/admin/EmptyState'
import {ProductEditorDialog} from '../components/admin/ProductEditorDialog'
import {Pagination} from '../components/admin/Pagination'

export function ProductsPage() {
  const [result, setResult] = useState<PageResponse<Product>>({content: [], page: 0, size: 20, totalElements: 0, totalPages: 0})
  const [search, setSearch] = useState('')
  const [active, setActive] = useState('')
  const [filters, setFilters] = useState({search: '', active: ''})
  const [categories, setCategories] = useState<CategoryOption[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [page, setPage] = useState(0)
  const [editor, setEditor] = useState<Product | null | undefined>(undefined)
  const [confirm, setConfirm] = useState<Product | null>(null)
  const [pending, setPending] = useState(false)
  const [feedback, setFeedback] = useState('')
  const sequence = useRef(0)

  useEffect(() => {
    let current = true
    const request = ++sequence.current
    setLoading(true)
    setError(false)
    listProducts({page, size: 20, q: filters.search || undefined, active: filters.active === '' ? undefined : filters.active === 'true'})
      .then((value) => { if (current && request === sequence.current) setResult(value) })
      .catch(() => { if (current && request === sequence.current) setError(true) })
      .finally(() => { if (current && request === sequence.current) setLoading(false) })
    return () => { current = false }
  }, [page, filters])

  useEffect(() => { listCategoryOptions().then(setCategories).catch(() => setCategories([])) }, [])

  function applyFilters(event: React.FormEvent) {
    event.preventDefault()
    setPage(0)
    setFilters({search, active})
  }

  function openEditor(product?: Product) { setFeedback(''); setEditor(product ?? null) }

  async function save(input: Parameters<typeof createProduct>[0] | UpdateProductInput) {
    setPending(true)
    try {
      if (editor) {
        const saved = await updateProduct(editor.id, input as UpdateProductInput)
        setResult((current) => ({...current, content: current.content.map((item) => item.id === saved.id ? saved : item)}))
        setFeedback('Product updated')
      } else {
        const saved = await createProduct(input as Parameters<typeof createProduct>[0])
        setResult((current) => ({...current, content: [saved, ...current.content], totalElements: current.totalElements + 1}))
        setFeedback('Product created')
      }
      setEditor(undefined)
    } catch (caught) {
      if (caught instanceof ApiError && caught.status === 409 && (!Array.isArray(caught.fieldErrors) || caught.fieldErrors.length === 0)) {
        if (caught.message.toLowerCase().includes('stale') || caught.message.toLowerCase().includes('revision')) {
          if (editor) await getProduct(editor.id)
          setEditor(undefined)
          setFeedback('Product changed by someone else. Reopen the editor to review the latest values.')
        }
      } else if (caught instanceof ApiError && Array.isArray(caught.fieldErrors) && caught.fieldErrors.length > 0) {
        setFeedback(caught.message)
      } else {
        setFeedback('Unable to save product.')
      }
    } finally { setPending(false) }
  }

  async function deactivate() {
    if (!confirm) return
    setPending(true)
    try {
      await deactivateProduct(confirm.id, confirm.revision)
      setResult((current) => ({...current, content: current.content.map((item) => item.id === confirm.id ? {...item, active: false} : item)}))
      setFeedback('Product deactivated')
      setConfirm(null)
    } finally { setPending(false) }
  }

  async function activate(product: Product) {
    setPending(true)
    try {
      await updateProduct(product.id, {
        revision: product.revision,
        name: product.name,
        description: product.description,
        price: product.price,
        stockQuantity: product.stockQuantity,
        imageUrl: product.imageUrl,
        active: true,
        categoryIds: product.categories.map((category) => category.id),
      })
      setResult((current) => ({...current, content: current.content.map((item) => item.id === product.id ? {...item, active: true, revision: item.revision + 1} : item)}))
      setFeedback('Product activated')
    } catch {
      setFeedback('Unable to activate product.')
    } finally {
      setPending(false)
    }
  }

  return (
    <section>
      <div className="flex items-start justify-between gap-4">
        <div><h2 className="text-2xl font-bold">Products</h2><p className="mt-2 text-base-content/70">Manage inventory, pricing, and product information.</p></div>
        <button type="button" className="btn btn-primary" onClick={() => openEditor()}>Create Product</button>
      </div>
      {feedback && <div className="alert alert-success mt-4" role="status">{feedback}</div>}
      <form className="my-6 flex flex-wrap items-end gap-3" onSubmit={applyFilters}>
        <label className="form-control"><span className="label-text">Search products</span><input className="input input-bordered" value={search} onChange={(event) => setSearch(event.target.value)} /></label>
        <label className="form-control"><span className="label-text">Status</span><select aria-label="Status" className="select select-bordered" value={active} onChange={(event) => setActive(event.target.value)}><option value="">All</option><option value="true">Active</option><option value="false">Inactive</option></select></label>
        <button type="submit" className="btn btn-secondary">Apply Filters</button>
      </form>
      {loading && <div role="status" className="alert alert-info">Loading products...</div>}
      {error && <div role="alert" className="alert alert-error"><span>Unable to load products.</span><button type="button" className="btn btn-sm" aria-label="Refresh products" onClick={() => setFilters({...filters})}>Refresh</button></div>}
      {!loading && !error && result.content.length === 0 && <><EmptyState title="No products found" actionLabel="Create Product" onAction={() => openEditor()} /><button type="button" className="btn btn-ghost mt-2" aria-label="Refresh products" onClick={() => setFilters({...filters})}>Refresh</button></>}
      {!loading && !error && result.content.length > 0 && <div className="overflow-x-auto"><table className="table"><thead><tr>{['SKU', 'Product', 'Price', 'Stock', 'Status', 'Categories', 'Actions'].map((heading) => <th key={heading} scope="col" className="whitespace-nowrap">{heading}</th>)}</tr></thead><tbody>{result.content.map((product) => <tr key={product.id}><td>{product.sku}</td><td>{product.name}</td><td>{product.price.toFixed(2)}</td><td>{product.stockQuantity}</td><td>{product.active ? 'Active' : 'Inactive'}</td><td>{product.categories.map((category) => category.name).join(', ')}</td><td className="flex gap-2"><button type="button" className="btn btn-ghost btn-sm" aria-label={`Edit ${product.name}`} onClick={() => openEditor(product)}>Edit</button>{product.active ? <button type="button" className="btn btn-ghost btn-sm" aria-label={`Deactivate ${product.name}`} onClick={() => setConfirm(product)}>Deactivate</button> : <button type="button" className="btn btn-ghost btn-sm" aria-label={`Activate ${product.name}`} onClick={() => activate(product)}>Activate</button>}</td></tr>)}</tbody></table><Pagination page={result.page + 1} totalPages={result.totalPages} onPageChange={(next) => setPage(next - 1)} /></div>}
      <ProductEditorDialog open={editor !== undefined} product={editor ?? undefined} categories={categories} onCancel={() => setEditor(undefined)} onSubmit={save} pending={pending} />
      <ConfirmDialog open={Boolean(confirm)} title="Deactivate product" message={`Deactivate ${confirm?.name ?? 'this product'}?`} confirmLabel="Deactivate" cancelLabel="Cancel" onConfirm={deactivate} onCancel={() => setConfirm(null)} />
    </section>
  )
}
