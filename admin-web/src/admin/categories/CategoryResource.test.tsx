import {render, screen} from '@testing-library/react'
import {AdminContext, ResourceContextProvider} from 'react-admin'
import {CategoryList} from './CategoryList'

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
    expect(consoleError).not.toHaveBeenCalled()
    expect(consoleWarn).not.toHaveBeenCalled()

    consoleError.mockRestore()
    consoleWarn.mockRestore()
  })
})
