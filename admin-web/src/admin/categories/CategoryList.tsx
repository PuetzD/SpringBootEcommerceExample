import {CreateButton, Datagrid, DeleteButton, EditButton, List, NumberField, Pagination, TextField} from 'react-admin'

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
        <TextField source="name" />
        <TextField source="slug" />
        <NumberField source="productCount" label="Products" />
        <EditButton label="Rename" />
        <DeleteButton
          mutationMode="pessimistic"
          confirmTitle="Delete category?"
          confirmContent="Categories assigned to products cannot be deleted."
        />
      </Datagrid>
    </List>
  )
}
