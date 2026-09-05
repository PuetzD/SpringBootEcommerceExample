import {Datagrid, DateField, List, NumberField, Pagination, TextField, TextInput} from 'react-admin'

const filters = [
  <TextInput key="q" source="q" label="Order number" alwaysOn />,
]

export function OrderList() {
  return (
    <List filters={filters} perPage={20} pagination={<Pagination rowsPerPageOptions={[5, 10, 20, 25, 50]} />}>
      <Datagrid rowClick="show" bulkActionButtons={false}>
        <TextField source="orderNumber" label="Order number" />
        <NumberField source="customerId" label="Customer" />
        <NumberField source="total" options={{style: 'currency', currency: 'EUR'}} />
        <DateField source="placedAt" showTime />
      </Datagrid>
    </List>
  )
}
