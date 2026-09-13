import {
  BooleanField,
  CreateButton,
  Datagrid,
  EditButton,
  FunctionField,
  List,
  NumberField,
  Pagination,
  SelectInput,
  TextField,
  TextInput,
} from 'react-admin'
import {ProductActions} from './ProductActions'

const filters = [
  <TextInput key="q" source="q" label="Search" alwaysOn />,
  <SelectInput
    key="active"
    source="active"
    label="Status"
    choices={[
      {id: 'true', name: 'Active'},
      {id: 'false', name: 'Inactive'},
    ]}
  />,
]

function ProductListActions() {
  return <CreateButton />
}

export function ProductList() {
  return (
    <List
      filters={filters}
      actions={<ProductListActions />}
      perPage={20}
      pagination={<Pagination rowsPerPageOptions={[5, 10, 20, 25, 50]} />}
    >
      <Datagrid rowClick="edit" bulkActionButtons={false}>
        <TextField source="sku" sortable={false} />
        <TextField source="name" sortable={false} />
        <NumberField
          source="price"
          sortable={false}
          options={{style: 'currency', currency: 'EUR'}}
        />
        <NumberField source="stockQuantity" sortable={false} />
        <BooleanField source="active" sortable={false} />
        <FunctionField
          source="categories"
          label="Categories"
          sortable={false}
          render={(record: {categories?: {name: string}[]}) =>
            record.categories?.map(({name}) => name).join(', ') ?? ''}
        />
        <EditButton />
        <ProductActions />
      </Datagrid>
    </List>
  )
}
