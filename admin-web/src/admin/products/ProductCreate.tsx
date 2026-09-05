import {AutocompleteArrayInput, Create, NumberInput, ReferenceArrayInput, SimpleForm, TextInput} from 'react-admin'

export function ProductCreate() {
  return (
    <Create>
      <SimpleForm>
        <TextInput source="sku" isRequired />
        <TextInput source="name" isRequired />
        <TextInput source="description" multiline />
        <NumberInput source="price" isRequired />
        <NumberInput source="stockQuantity" isRequired />
        <TextInput source="imageUrl" />
        <ReferenceArrayInput source="categoryIds" reference="categories">
          <AutocompleteArrayInput optionText="name" />
        </ReferenceArrayInput>
      </SimpleForm>
    </Create>
  )
}
