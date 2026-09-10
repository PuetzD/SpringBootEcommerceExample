import {
  CreateButton,
  Datagrid,
  DeleteButton,
  EditButton,
  List,
  NumberField,
  Pagination,
  TextField,
  useNotify,
  useRefresh,
  WrapperField,
} from 'react-admin'

function conflictCode(error: unknown): string | undefined {
  if (typeof error !== 'object' || error === null) return undefined
  const code = (error as {code?: unknown}).code
  return typeof code === 'string' ? code : undefined
}

function CategoryDeleteButton() {
  const notify = useNotify()
  const refresh = useRefresh()

  return (
    <DeleteButton
      mutationMode="pessimistic"
      confirmTitle="Delete category?"
      confirmContent="Categories assigned to products cannot be deleted."
      mutationOptions={{
        onError: (error) => {
          if (conflictCode(error) === 'catalog.category.in-use') {
            notify('Remove this category from every product before deleting it.', {type: 'error'})
            refresh()
            return
          }
          if (conflictCode(error) === 'catalog.category.stale') {
            notify('Category changed on the server. The list was refreshed; choose the action again.', {type: 'error'})
            refresh()
            return
          }
          notify('Unable to delete category', {type: 'error'})
        },
      }}
    />
  )
}

function CategoryListActions() {
  return <CreateButton />
}

export function CategoryList() {
  return (
    <List
      actions={<CategoryListActions />}
      perPage={20}
      pagination={<Pagination rowsPerPageOptions={[5, 10, 20, 25, 50]} />}
    >
      <Datagrid rowClick="edit" bulkActionButtons={false}>
        <TextField source="name" sortable={false} />
        <TextField source="slug" sortable={false} />
        <NumberField source="productCount" label="Products" sortable={false} />
        <WrapperField label="Actions">
          <EditButton label="Rename" />
          <CategoryDeleteButton />
        </WrapperField>
      </Datagrid>
    </List>
  )
}
