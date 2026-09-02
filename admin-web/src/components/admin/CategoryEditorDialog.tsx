import {FormEvent, useEffect, useRef, useState} from 'react'
import type {Category, CreateCategoryInput, RenameCategoryInput} from '../../api/types'

const emptyFieldErrors: Record<string, string> = {}

type Props = {
  open: boolean
  category?: Category
  onCancel: () => void
  onSubmit: (input: CreateCategoryInput | RenameCategoryInput) => void | Promise<void>
  fieldErrors?: Record<string, string>
  pending?: boolean
}

export function CategoryEditorDialog({
  open,
  category,
  onCancel,
  onSubmit,
  fieldErrors = emptyFieldErrors,
  pending = false,
}: Props) {
  const [name, setName] = useState(category?.name ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const nameInput = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (!open) {
      return
    }
    setName(category?.name ?? '')
    setErrors(fieldErrors)
  }, [open, category, fieldErrors])

  useEffect(() => {
    if (errors.name) {
      nameInput.current?.focus()
    }
  }, [errors])

  if (!open) {
    return null
  }

  function updateName(value: string) {
    setName(value)
    setErrors((current) => {
      const remaining = {...current}
      delete remaining.name
      return remaining
    })
  }

  function submit(event: FormEvent) {
    event.preventDefault()
    if (!name.trim()) {
      setErrors({name: 'Name is required'})
      return
    }
    void onSubmit(category ? {name: name.trim(), revision: category.revision} : {name: name.trim()})
  }

  return (
    <dialog
      open
      className="modal modal-open"
      aria-labelledby="category-dialog-title"
      onCancel={(event) => {
        event.preventDefault()
        onCancel()
      }}
    >
      <div className="modal-box max-w-md">
        <h2 id="category-dialog-title" className="text-lg font-bold">
          {category ? 'Rename Category' : 'Create Category'}
        </h2>
        <form onSubmit={submit} className="mt-4 space-y-4">
          <label className="form-control" htmlFor="category-name">
            <span className="label-text">Name</span>
            <input
              ref={nameInput}
              id="category-name"
              name="name"
              className="input input-bordered"
              value={name}
              aria-invalid={Boolean(errors.name)}
              aria-describedby={errors.name ? 'category-name-error' : undefined}
              onChange={(event) => updateName(event.target.value)}
            />
            {errors.name && (
              <span id="category-name-error" className="text-error">
                {errors.name}
              </span>
            )}
          </label>
          <div className="modal-action">
            <button type="button" className="btn btn-ghost" onClick={onCancel} disabled={pending}>
              Cancel
            </button>
            <button
              type="submit"
              className={`btn btn-primary ${pending ? 'loading' : ''}`}
              disabled={pending}
            >
              {category ? 'Save Changes' : 'Create Category'}
            </button>
          </div>
        </form>
      </div>
    </dialog>
  )
}
