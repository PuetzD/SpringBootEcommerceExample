import {fireEvent, render, screen, waitFor, within} from '@testing-library/react'
import {AdminContext, HttpError, Notification, ResourceContextProvider} from 'react-admin'
import {CategoryCreate} from '../../../admin/categories/CategoryCreate'
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
    const remove = vi.fn().mockRejectedValue(
      Object.assign(new HttpError('Conflict', 409), {code: 'catalog.category.in-use'}),
    )

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
  })

  it('refreshes after a stale delete instead of retrying the old action', async () => {
    const remove = vi.fn().mockRejectedValue(
      Object.assign(new HttpError('Conflict', 409), {code: 'catalog.category.stale'}),
    )
    const getList = vi.fn().mockResolvedValue({
      data: [{id: 7, name: 'Networking', slug: 'networking', revision: 3, productCount: 4}],
      total: 1,
    })

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
    await waitFor(() => expect(getList.mock.calls.length).toBeGreaterThan(1))
    expect(remove).toHaveBeenCalledTimes(1)
  })
})
