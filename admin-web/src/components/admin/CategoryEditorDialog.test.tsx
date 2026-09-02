import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {CategoryEditorDialog} from './CategoryEditorDialog'

const category = {
  id: 7,
  name: 'Tools',
  slug: 'tools',
  revision: 3,
  productCount: 2,
}

describe('CategoryEditorDialog', () => {
  it('submits a labelled category name when creating a category', async () => {
    const onSubmit = vi.fn()
    render(<CategoryEditorDialog open={true} onCancel={vi.fn()} onSubmit={onSubmit}/>)

    fireEvent.change(screen.getByLabelText(/^name$/i), {target: {value: 'Tools'}})
    fireEvent.click(screen.getByRole('button', {name: /create category/i}))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith({name: 'Tools'}))
  })

  it('submits the loaded revision when renaming a category', async () => {
    const onSubmit = vi.fn()
    render(
      <CategoryEditorDialog
        open={true}
        category={category}
        onCancel={vi.fn()}
        onSubmit={onSubmit}
      />,
    )

    fireEvent.change(screen.getByLabelText(/^name$/i), {target: {value: 'Garden tools'}})
    fireEvent.click(screen.getByRole('button', {name: /save changes/i}))

    await waitFor(() =>
      expect(onSubmit).toHaveBeenCalledWith({name: 'Garden tools', revision: 3}),
    )
  })

  it('keeps field errors associated with the name input', () => {
    render(
      <CategoryEditorDialog
        open={true}
        onCancel={vi.fn()}
        onSubmit={vi.fn()}
        fieldErrors={{name: 'Name is already in use'}}
      />,
    )

    expect(screen.getByRole('textbox', {name: /name/i}).getAttribute('aria-invalid')).toBe('true')
    expect(screen.getByText('Name is already in use')).toBeTruthy()
  })
})
