import {Edit, required, SimpleForm, TextInput} from 'react-admin'

export function CategoryEdit() {
  return (
    <Edit mutationMode="pessimistic">
      <SimpleForm>
        <TextInput source="name" validate={required('Required field')} />
        <TextInput source="slug" disabled />
        <TextInput source="productCount" disabled />
        <TextInput source="revision" disabled />
      </SimpleForm>
    </Edit>
  )
}
