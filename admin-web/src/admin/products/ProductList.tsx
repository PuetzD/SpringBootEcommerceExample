import {
  BooleanField,
  CreateButton,
  Datagrid,
  EditButton,
  FunctionField,
  List,
  NumberField,
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
    <List filters={filters} actions={<ProductListActions />} perPage={20}>
      <Datagrid rowClick="edit" bulkActionButtons={false}>
        <TextField source="sku" />
        <TextField source="name" />
        <NumberField source="price" options={{style: 'currency', currency: 'EUR'}} />
        <NumberField source="stockQuantity" />
        <BooleanField source="active" />
        <FunctionField
          source="categories"
          label="Categories"
          render={(record: {categories?: {name: string}[]}) =>
            record.categories?.map(({name}) => name).join(', ') ?? ''}
        />
        <EditButton />
        <ProductActions />
      </Datagrid>
    </List>
  )
}
