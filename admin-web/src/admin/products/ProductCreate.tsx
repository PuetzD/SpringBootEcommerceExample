import {AutocompleteArrayInput, Create, NumberInput, ReferenceArrayInput, SimpleForm, TextInput, required} from 'react-admin'

export function ProductCreate() {
  return (
    <Create>
      <SimpleForm>
        <TextInput source="sku" validate={required('Required field')} />
        <TextInput source="name" validate={required('Required field')} />
        <TextInput source="description" multiline />
        <NumberInput source="price" validate={required('Required field')} />
        <NumberInput source="stockQuantity" validate={required('Required field')} />
        <TextInput source="imageUrl" />
        <ReferenceArrayInput source="categoryIds" reference="categoryOptions">
          <AutocompleteArrayInput optionText="name" />
        </ReferenceArrayInput>
      </SimpleForm>
    </Create>
  )
}
