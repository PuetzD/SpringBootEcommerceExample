import {Create, SimpleForm, TextInput} from 'react-admin'

export function CategoryCreate() {
  return (
    <Create>
      <SimpleForm>
        <TextInput source="name" isRequired />
      </SimpleForm>
    </Create>
  )
}
