import {fireEvent, render, screen, waitFor, within} from '@testing-library/react'
import {AdminContext, HttpError, Notification, ResourceContextProvider} from 'react-admin'
import {MemoryRouter, Route, Routes} from 'react-router-dom'
import {CategoryCreate} from '../../../admin/categories/CategoryCreate'
import {CategoryEdit} from '../../../admin/categories/CategoryEdit'
import {CategoryList} from '../../../admin/categories/CategoryList'

describe('Category resource', () => {
  it('renders category product counts from the data provider', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    const consoleWarn = vi.spyOn(console, 'warn').mockImplementation(() => {})

    render(
      <AdminContext
        dataProvider={{
          getList: vi.fn().mockResolvedValue({
            data: [{id: 7, name: 'Networking', slug: 'networking', revision: 2, productCount: 4}],
            total: 1,
          }),
        }}
      >
        <ResourceContextProvider value="categories">
          <CategoryList />
        </ResourceContextProvider>
      </AdminContext>,
    )

    expect(await screen.findByText('Networking')).toBeTruthy()
    expect(screen.getByText('4')).toBeTruthy()
    expect(screen.getByRole('columnheader', {name: /fields\.name$/i})).toBeTruthy()
    expect(screen.getByRole('columnheader', {name: /fields\.slug$/i})).toBeTruthy()
    expect(screen.getByRole('columnheader', {name: /^products$/i})).toBeTruthy()
    expect(screen.getByRole('columnheader', {name: /^actions$/i})).toBeTruthy()
    expect(within(screen.getByRole('table')).queryByRole('button', {name: 'ra.sort.sort_by'})).toBeNull()
    expect(consoleError).not.toHaveBeenCalled()
    expect(consoleWarn).not.toHaveBeenCalled()

    consoleError.mockRestore()
    consoleWarn.mockRestore()
  })

  it('announces a required category name before create submission', async () => {
    const create = vi.fn().mockResolvedValue({data: {id: 7, name: 'Networking', slug: 'networking'}})

    render(
      <AdminContext dataProvider={{create, getList: vi.fn().mockResolvedValue({data: [], total: 0})}}>
        <ResourceContextProvider value="categories">
          <CategoryCreate />
        </ResourceContextProvider>
      </AdminContext>,
    )

    fireEvent.change(screen.getByRole('textbox', {name: /name/i}), {target: {value: ''}})
    const form = (await screen.findByRole('button', {name: /save/i})).closest('form')
    if (!form) throw new Error('Category create form is missing')
    fireEvent.submit(form)

    await waitFor(() => expect(screen.getByText('Required field')).toBeTruthy())
    expect(create).not.toHaveBeenCalled()
  })

  it('keeps an in-use category visible and gives safe remediation after confirmed delete', async () => {
    const inUseError = Object.assign(new HttpError('Conflict', 409), {code: 'catalog.category.in-use'})
    const remove = vi.fn().mockRejectedValue(inUseError)
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    try {
      render(
        <AdminContext
          dataProvider={{
            delete: remove,
            getList: vi.fn().mockResolvedValue({
              data: [{id: 7, name: 'Networking', slug: 'networking', revision: 2, productCount: 4}],
              total: 1,
            }),
          }}
        >
          <ResourceContextProvider value="categories">
            <CategoryList />
          </ResourceContextProvider>
          <Notification />
        </AdminContext>,
      )

      const row = (await screen.findByText('Networking')).closest('tr')
      if (!row) throw new Error('Category row is missing')
      fireEvent.click(within(row).getByRole('button', {name: /delete/i}))
      fireEvent.click(screen.getByRole('button', {name: /confirm/i}))

      expect(await screen.findByText(/remove this category from every product/i)).toBeTruthy()
      expect(screen.getByText('Networking')).toBeTruthy()
      expect(remove).toHaveBeenCalledTimes(1)
      expect(consoleError).toHaveBeenCalledTimes(1)
      expect(consoleError).toHaveBeenCalledWith(inUseError)
    } finally {
      consoleError.mockRestore()
    }
  })

  it('refreshes after a stale delete instead of retrying the old action', async () => {
    const staleError = Object.assign(new HttpError('Conflict', 409), {code: 'catalog.category.stale'})
    const originalCategory = {id: 7, name: 'Networking', slug: 'networking', revision: 2, productCount: 4}
    const refreshedCategory = {id: 7, name: 'Network equipment', slug: 'network-equipment', revision: 3, productCount: 4}
    const remove = vi.fn().mockRejectedValueOnce(staleError).mockResolvedValueOnce({data: refreshedCategory})
    const getList = vi.fn().mockResolvedValueOnce({
      data: [originalCategory],
      total: 1,
    })
    getList.mockResolvedValue({data: [refreshedCategory], total: 1})
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    try {
      render(
        <AdminContext dataProvider={{delete: remove, getList}}>
          <ResourceContextProvider value="categories">
            <CategoryList />
          </ResourceContextProvider>
          <Notification />
        </AdminContext>,
      )

      const row = (await screen.findByText('Networking')).closest('tr')
      if (!row) throw new Error('Category row is missing')
      fireEvent.click(within(row).getByRole('button', {name: /delete/i}))
      fireEvent.click(screen.getByRole('button', {name: /confirm/i}))

      expect(await screen.findByText(/choose the action again/i)).toBeTruthy()
      expect(await screen.findByText('Network equipment')).toBeTruthy()
      expect(consoleError).toHaveBeenCalledTimes(1)
      expect(consoleError).toHaveBeenCalledWith(staleError)

      const refreshedRow = screen.getByText('Network equipment').closest('tr')
      if (!refreshedRow) throw new Error('Refreshed category row is missing')
      await waitFor(() => expect(within(refreshedRow).getByRole('button', {name: /delete/i})).toBeTruthy())
      fireEvent.click(within(refreshedRow).getByRole('button', {name: /delete/i}))
      fireEvent.click(screen.getByRole('button', {name: /confirm/i}))

      await waitFor(() => expect(remove).toHaveBeenCalledTimes(2))
      expect(remove).toHaveBeenNthCalledWith(
        2,
        'categories',
        expect.objectContaining({id: 7, previousData: refreshedCategory}),
      )
    } finally {
      consoleError.mockRestore()
    }
  })

  it('refreshes an edit form after a stale rename before a user submits a new action', async () => {
    const staleError = Object.assign(new HttpError('Conflict', 409), {code: 'catalog.category.stale'})
    const originalCategory = {id: 7, name: 'Networking', slug: 'networking', revision: 2, productCount: 4}
    const refreshedCategory = {id: 7, name: 'Network equipment', slug: 'network-equipment', revision: 3, productCount: 4}
    const getOne = vi.fn().mockResolvedValueOnce({data: originalCategory})
    getOne.mockResolvedValue({data: refreshedCategory})
    const update = vi
      .fn()
      .mockRejectedValueOnce(staleError)
      .mockResolvedValueOnce({data: {...refreshedCategory, name: 'Managed network equipment'}})
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    try {
      render(
        <MemoryRouter initialEntries={['/categories/7']}>
          <AdminContext dataProvider={{getOne, update}}>
            <ResourceContextProvider value="categories">
              <Routes>
                <Route path="/categories/:id" element={<CategoryEdit />} />
              </Routes>
            </ResourceContextProvider>
            <Notification />
          </AdminContext>
        </MemoryRouter>,
      )

      const name = await screen.findByRole('textbox', {name: /name/i})
      expect(name.getAttribute('value')).toBe('Networking')
      fireEvent.change(name, {target: {value: 'Attempted rename'}})
      fireEvent.submit(name.closest('form')!)

      expect(await screen.findByText(/choose the action again/i)).toBeTruthy()
      await waitFor(() => expect(getOne).toHaveBeenCalledTimes(2))
      expect(screen.getByRole('textbox', {name: /name/i}).getAttribute('value')).toBe('Network equipment')
      expect(screen.getByRole('textbox', {name: /revision/i}).getAttribute('value')).toBe('3')
      expect(consoleError).toHaveBeenCalledTimes(1)
      expect(consoleError).toHaveBeenCalledWith(staleError)

      fireEvent.change(screen.getByRole('textbox', {name: /name/i}), {target: {value: 'Managed network equipment'}})
      fireEvent.submit(screen.getByRole('textbox', {name: /name/i}).closest('form')!)

      await waitFor(() => expect(update).toHaveBeenCalledTimes(2))
      expect(update).toHaveBeenNthCalledWith(
        2,
        'categories',
        expect.objectContaining({
          data: expect.objectContaining({name: 'Managed network equipment', revision: 3}),
          previousData: expect.objectContaining({revision: 3}),
        }),
      )
    } finally {
      consoleError.mockRestore()
    }
  })

  it('keeps a rejected rename draft and exposes its field error without a generic notification', async () => {
    const validationError = new HttpError('Invalid category name', 422, {
      errors: {name: 'A category with this name already exists'},
      fieldErrors: [{field: 'name', message: 'A category with this name already exists'}],
    })
    const originalCategory = {id: 7, name: 'Networking', slug: 'networking', revision: 2, productCount: 4}
    const update = vi.fn().mockRejectedValue(validationError)
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    try {
      render(
        <MemoryRouter initialEntries={['/categories/7']}>
          <AdminContext dataProvider={{getOne: vi.fn().mockResolvedValue({data: originalCategory}), update}}>
            <ResourceContextProvider value="categories">
              <Routes>
                <Route path="/categories/:id" element={<CategoryEdit />} />
              </Routes>
            </ResourceContextProvider>
            <Notification />
          </AdminContext>
        </MemoryRouter>,
      )

      const name = await screen.findByRole('textbox', {name: /name/i})
      fireEvent.change(name, {target: {value: 'Duplicate networking'}})
      fireEvent.submit(name.closest('form')!)

      const fieldError = await screen.findByText('A category with this name already exists')
      await waitFor(() => expect(update).toHaveBeenCalledTimes(1))
      expect(name.getAttribute('value')).toBe('Duplicate networking')
      expect(name.getAttribute('aria-describedby')).toBe(fieldError.getAttribute('id'))
      expect(screen.queryByText('Unable to rename category')).toBeNull()
      expect(consoleError).toHaveBeenCalledTimes(1)
      expect(consoleError).toHaveBeenCalledWith(validationError)
    } finally {
      consoleError.mockRestore()
    }
  })

  it('does not expose an unguarded Category delete action on the edit form', async () => {
    render(
      <MemoryRouter initialEntries={['/categories/7']}>
        <AdminContext
          dataProvider={{
            getOne: vi.fn().mockResolvedValue({
              data: {id: 7, name: 'Networking', slug: 'networking', revision: 2, productCount: 4},
            }),
          }}
        >
          <ResourceContextProvider value="categories">
            <Routes>
              <Route path="/categories/:id" element={<CategoryEdit />} />
            </Routes>
          </ResourceContextProvider>
        </AdminContext>
      </MemoryRouter>,
    )

    await screen.findByDisplayValue('Networking')
    expect(screen.queryByRole('button', {name: /delete/i})).toBeNull()
  })
})
