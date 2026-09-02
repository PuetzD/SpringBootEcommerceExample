import {FormEvent, useEffect, useRef, useState} from 'react'
import type {CategoryOption, CreateProductInput, Product, UpdateProductInput} from '../../api/types'

type Props = {
    open: boolean
    product?: Product
    categories: CategoryOption[]
    onCancel: () => void
    onSubmit: (input: CreateProductInput | UpdateProductInput) => void | Promise<void>
    fieldErrors?: Record<string, string>
    pending?: boolean
}

export function ProductEditorDialog({
    open,
    product,
    categories,
    onCancel,
    onSubmit,
    fieldErrors = {},
    pending = false,
}: Props) {
    const [values, setValues] = useState(() => ({
        sku: product?.sku ?? '',
        name: product?.name ?? '',
        description: product?.description ?? '',
        price: product ? String(product.price) : '',
        stockQuantity: product ? String(product.stockQuantity) : '',
        imageUrl: product?.imageUrl ?? '',
        active: product?.active ?? true,
        categoryIds: product?.categories.map((category) => category.id) ?? [],
    }))
    const [errors, setErrors] = useState<Record<string, string>>({})
    const firstInvalid = useRef<HTMLInputElement | HTMLTextAreaElement | null>(null)

    useEffect(() => {
        if (!open) return
        setValues({
            sku: product?.sku ?? '',
            name: product?.name ?? '',
            description: product?.description ?? '',
            price: product ? String(product.price) : '',
            stockQuantity: product ? String(product.stockQuantity) : '',
            imageUrl: product?.imageUrl ?? '',
            active: product?.active ?? true,
            categoryIds: product?.categories.map((category) => category.id) ?? [],
        })
        setErrors({})
    }, [open, product])

    useEffect(() => {
        if (Object.keys(errors).length > 0) firstInvalid.current?.focus()
    }, [errors])

    if (!open) return null

    function update(name: string, value: string | boolean) {
        setValues((current) => ({...current, [name]: value}))
    }

    function toggleCategory(id: number) {
        setValues((current) => ({
            ...current,
            categoryIds: current.categoryIds.includes(id)
                ? current.categoryIds.filter((categoryId) => categoryId !== id)
                : [...current.categoryIds, id],
        }))
    }

    function submit(event: FormEvent) {
        event.preventDefault()
        const nextErrors: Record<string, string> = {...fieldErrors}
        if (!product && !values.sku.trim()) nextErrors.sku = 'SKU is required'
        if (!values.name.trim()) nextErrors.name = 'Name is required'
        if (!values.price || Number(values.price) <= 0) nextErrors.price = 'Price is required'
        if (!values.stockQuantity || Number(values.stockQuantity) < 0) nextErrors.stockQuantity = 'Stock is required'
        setErrors(nextErrors)
        if (Object.keys(nextErrors).length > 0) return
        const common = {
            name: values.name.trim(),
            description: values.description.trim() || null,
            price: Number(values.price),
            stockQuantity: Number(values.stockQuantity),
            imageUrl: values.imageUrl.trim() || null,
            categoryIds: values.categoryIds,
        }
        void onSubmit(product ? {...common, revision: product.revision, active: values.active} : {...common, sku: values.sku.trim()})
    }

    const inputError = (name: string) => errors[name] ? `${name}-error` : undefined
    const field = (name: string) => errors[name]

    return (
        <dialog open className="modal modal-open" aria-labelledby="product-dialog-title">
            <div className="modal-box max-w-2xl">
                <h2 id="product-dialog-title" className="text-lg font-bold">
                    {product ? 'Edit Product' : 'Create Product'}
                </h2>
                <form onSubmit={submit} className="mt-4 space-y-4">
                    <label className="form-control">
                        <span className="label-text">SKU</span>
                        <input
                            aria-invalid={Boolean(field('sku'))}
                            aria-describedby={inputError('sku')}
                            ref={(element) => { if (!firstInvalid.current && field('sku')) firstInvalid.current = element }}
                            className="input input-bordered"
                            value={values.sku}
                            readOnly={Boolean(product)}
                            onChange={(event) => update('sku', event.target.value)}
                        />
                        {field('sku') && <span id="sku-error" className="text-error">{field('sku')}</span>}
                    </label>
                    <label className="form-control">
                        <span className="label-text">Name</span>
                        <input
                            aria-invalid={Boolean(field('name'))}
                            aria-describedby={inputError('name')}
                            className="input input-bordered"
                            value={values.name}
                            onChange={(event) => update('name', event.target.value)}
                        />
                        {field('name') && <span id="name-error" className="text-error">{field('name')}</span>}
                    </label>
                    <label className="form-control">
                        <span className="label-text">Description</span>
                        <textarea className="textarea textarea-bordered" value={values.description} onChange={(event) => update('description', event.target.value)} />
                    </label>
                    <label className="form-control">
                        <span className="label-text">Price</span>
                        <input aria-label="Price" type="number" step="0.01" min="0.01" className="input input-bordered" value={values.price} onChange={(event) => update('price', event.target.value)} />
                        {field('price') && <span className="text-error">{field('price')}</span>}
                    </label>
                    <label className="form-control">
                        <span className="label-text">Stock quantity</span>
                        <input aria-label="Stock" type="number" step="1" min="0" className="input input-bordered" value={values.stockQuantity} onChange={(event) => update('stockQuantity', event.target.value)} />
                        {field('stockQuantity') && <span className="text-error">{field('stockQuantity')}</span>}
                    </label>
                    <label className="form-control">
                        <span className="label-text">Image URL</span>
                        <input className="input input-bordered" value={values.imageUrl} onChange={(event) => update('imageUrl', event.target.value)} />
                    </label>
                    {product && <label className="label cursor-pointer justify-start gap-3"><input aria-label="Active" type="checkbox" className="checkbox" checked={values.active} onChange={(event) => update('active', event.target.checked)} /><span>Active</span></label>}
                    <fieldset>
                        <legend className="font-semibold">Categories</legend>
                        <div className="grid gap-2 sm:grid-cols-2">
                            {categories.map((category) => <label key={category.id} className="label cursor-pointer justify-start gap-2"><input aria-label={category.name} type="checkbox" className="checkbox" checked={values.categoryIds.includes(category.id)} onChange={() => toggleCategory(category.id)} /><span>{category.name}</span></label>)}
                        </div>
                    </fieldset>
                    <div className="modal-action">
                        <button type="button" className="btn btn-ghost" onClick={onCancel} disabled={pending}>Cancel</button>
                        <button type="submit" className={`btn btn-primary ${pending ? 'loading' : ''}`} disabled={pending}>{product ? 'Save Changes' : 'Create Product'}</button>
                    </div>
                </form>
            </div>
        </dialog>
    )
}
