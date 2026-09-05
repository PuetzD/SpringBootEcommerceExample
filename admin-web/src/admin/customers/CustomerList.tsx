import {Datagrid, List, Pagination, TextField, TextInput} from 'react-admin'

const filters = [
  <TextInput key="q" source="q" label="Email or name" alwaysOn />,
]

export function CustomerList() {
  return (
    <List
      filters={filters}
      perPage={20}
      pagination={<Pagination rowsPerPageOptions={[5, 10, 20, 25, 50]} />}
    >
      <Datagrid rowClick="show" bulkActionButtons={false}>
        <TextField source="id" label="Customer ID" />
        <TextField source="givenName" label="First name" />
        <TextField source="familyName" label="Last name" />
        <TextField source="contactEmail" label="Email" />
      </Datagrid>
    </List>
  )
}
