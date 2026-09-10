import {Create, required, SimpleForm, TextInput} from 'react-admin'

export function CategoryCreate() {
  return (
    <Create>
      <SimpleForm>
        <TextInput source="name" validate={required('Required field')} />
      </SimpleForm>
    </Create>
  )
}
