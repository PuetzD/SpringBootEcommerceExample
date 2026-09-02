import {useEffect, useRef, useState} from 'react'
import {ApiError} from '../api/client'
import {
  createCategory,
  deleteCategory,
  getCategory,
  listCategories,
  renameCategory,
} from '../api/catalog'
import type {Category, CreateCategoryInput, PageResponse, RenameCategoryInput} from '../api/types'
import {CategoryEditorDialog} from '../components/admin/CategoryEditorDialog'
import {ConfirmDialog} from '../components/admin/ConfirmDialog'
import {EmptyState} from '../components/admin/EmptyState'
import {FeedbackBanner} from '../components/admin/FeedbackBanner'
import {Pagination} from '../components/admin/Pagination'

type Feedback = {
  message: string
  tone: 'success' | 'error'
}

const emptyResult: PageResponse<Category> = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
}

function fieldErrors(error: ApiError): Record<string, string> {
  return Object.fromEntries(error.fieldErrors.map((fieldError) => [fieldError.field, fieldError.message]))
}

export function CategoriesPage() {
  const [result, setResult] = useState<PageResponse<Category>>(emptyResult)
  const [page, setPage] = useState(0)
  const [reload, setReload] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [editor, setEditor] = useState<Category | null | undefined>(undefined)
  const [editorErrors, setEditorErrors] = useState<Record<string, string>>({})
  const [confirm, setConfirm] = useState<Category | null>(null)
  const [pending, setPending] = useState(false)
  const [feedback, setFeedback] = useState<Feedback | null>(null)
  const sequence = useRef(0)
  const restoreFocus = useRef<HTMLElement | null>(null)
  const editorWasOpen = useRef(false)

  useEffect(() => {
    let current = true
    const request = ++sequence.current
    setLoading(true)
    setError(false)
    listCategories({page, size: 20})
      .then((value) => {
        if (current && request === sequence.current) {
          setResult(value)
        }
      })
      .catch(() => {
        if (current && request === sequence.current) {
          setError(true)
        }
      })
      .finally(() => {
        if (current && request === sequence.current) {
          setLoading(false)
        }
      })
    return () => {
      current = false
    }
  }, [page, reload])

  useEffect(() => {
    const editorOpen = editor !== undefined
    if (!editorOpen && editorWasOpen.current) {
      restoreFocus.current?.focus()
    }
    editorWasOpen.current = editorOpen
  }, [editor])

  function openEditor(category: Category | null, trigger: HTMLElement) {
    restoreFocus.current = trigger
    setFeedback(null)
    setEditorErrors({})
    setEditor(category)
  }

  function closeEditor() {
    setEditor(undefined)
    setEditorErrors({})
  }

  async function refreshCategory(id: number) {
    const refreshed = await getCategory(id)
    setResult((current) => ({
      ...current,
      content: current.content.map((category) => (category.id === id ? refreshed : category)),
    }))
  }

  async function save(input: CreateCategoryInput | RenameCategoryInput) {
    setPending(true)
    try {
      if (editor) {
        const saved = await renameCategory(editor.id, input as RenameCategoryInput)
        setResult((current) => ({
          ...current,
          content: current.content.map((category) => (category.id === saved.id ? saved : category)),
        }))
        setFeedback({message: 'Category renamed', tone: 'success'})
      } else {
        const saved = await createCategory(input as CreateCategoryInput)
        setResult((current) => ({
          ...current,
          content: [saved, ...current.content],
          totalElements: current.totalElements + 1,
          totalPages: Math.max(1, current.totalPages),
        }))
        setFeedback({message: 'Category created', tone: 'success'})
      }
      closeEditor()
    } catch (caught) {
      if (caught instanceof ApiError && caught.code === 'catalog.category.stale' && editor) {
        try {
          await refreshCategory(editor.id)
          closeEditor()
          setFeedback({
            message: 'Category changed by someone else. Reopen the editor to review the latest values.',
            tone: 'error',
          })
        } catch {
          setFeedback({message: 'Unable to refresh category.', tone: 'error'})
        }
      } else if (caught instanceof ApiError && caught.fieldErrors.length > 0) {
        setEditorErrors(fieldErrors(caught))
      } else {
        setFeedback({message: 'Unable to save category.', tone: 'error'})
      }
    } finally {
      setPending(false)
    }
  }

  async function remove() {
    if (!confirm) {
      return
    }
    setPending(true)
    try {
      await deleteCategory(confirm.id, confirm.revision)
      const wasLastOnPage = result.content.length === 1
      if (wasLastOnPage && result.page > 0) {
        setPage(result.page - 1)
      } else {
        setResult((current) => ({
          ...current,
          content: current.content.filter((category) => category.id !== confirm.id),
          totalElements: Math.max(0, current.totalElements - 1),
        }))
      }
      setFeedback({message: 'Category deleted', tone: 'success'})
      setConfirm(null)
    } catch (caught) {
      if (caught instanceof ApiError && caught.code === 'catalog.category.in-use') {
        setFeedback({
          message: 'Remove this category from every product before deleting it.',
          tone: 'error',
        })
      } else if (caught instanceof ApiError && caught.code === 'catalog.category.stale') {
        try {
          await refreshCategory(confirm.id)
          setFeedback({
            message: 'Category changed by someone else. Review the latest values before trying again.',
            tone: 'error',
          })
        } catch {
          setFeedback({message: 'Unable to refresh category.', tone: 'error'})
        }
      } else {
        setFeedback({message: 'Unable to delete category.', tone: 'error'})
      }
      setConfirm(null)
    } finally {
      setPending(false)
    }
  }

  return (
    <section>
      <div className="flex items-start justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold">Categories</h2>
          <p className="mt-2 text-base-content/70">Organize your catalog into product groups.</p>
        </div>
        <button
          type="button"
          className="btn btn-primary"
          onClick={(event) => openEditor(null, event.currentTarget)}
        >
          Create Category
        </button>
      </div>
      {feedback && <FeedbackBanner title="Categories" message={feedback.message} tone={feedback.tone}/>}
      {loading && <div role="status" className="alert alert-info mt-4">Loading categories...</div>}
      {error && (
        <div role="alert" className="alert alert-error mt-4">
          <span>Unable to load categories.</span>
          <button type="button" className="btn btn-sm" aria-label="Refresh categories" onClick={() => setReload((current) => current + 1)}>
            Refresh
          </button>
        </div>
      )}
      {!loading && !error && result.content.length === 0 && (
        <>
          <EmptyState title="No categories found" actionLabel="Create Category" onAction={() => openEditor(null, document.activeElement as HTMLElement)}/>
          <button type="button" className="btn btn-ghost mt-2" aria-label="Refresh categories" onClick={() => setReload((current) => current + 1)}>
            Refresh
          </button>
        </>
      )}
      {!loading && !error && result.content.length > 0 && (
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                {['Name', 'Slug', 'Products', 'Actions'].map((heading) => (
                  <th key={heading} scope="col">{heading}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {result.content.map((category) => (
                <tr key={category.id}>
                  <td>{category.name}</td>
                  <td>{category.slug}</td>
                  <td>{category.productCount}</td>
                  <td className="flex gap-2">
                    <button type="button" className="btn btn-ghost btn-sm" aria-label={`Rename ${category.name}`} onClick={(event) => openEditor(category, event.currentTarget)}>
                      Rename
                    </button>
                    <button type="button" className="btn btn-ghost btn-sm" aria-label={`Delete ${category.name}`} onClick={() => setConfirm(category)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination page={result.page + 1} totalPages={result.totalPages} onPageChange={(next) => setPage(next - 1)}/>
        </div>
      )}
      <CategoryEditorDialog
        open={editor !== undefined}
        category={editor ?? undefined}
        onCancel={closeEditor}
        onSubmit={save}
        fieldErrors={editorErrors}
        pending={pending}
      />
      <ConfirmDialog
        open={Boolean(confirm)}
        title="Delete category"
        message={`Delete ${confirm?.name ?? 'this category'}? This action cannot be undone.`}
        confirmLabel="Delete Category"
        cancelLabel="Cancel"
        onConfirm={remove}
        onCancel={() => setConfirm(null)}
      />
    </section>
  )
}
