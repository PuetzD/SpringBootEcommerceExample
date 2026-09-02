import {AutocompleteArrayInput, BooleanInput, Edit, NumberInput, ReferenceArrayInput, SimpleForm, TextInput} from 'react-admin'

export function ProductEdit() {
  return (
    <Edit mutationMode="pessimistic">
      <SimpleForm>
        <TextInput source="sku" disabled />
        <TextInput source="name" isRequired />
        <TextInput source="description" multiline />
        <NumberInput source="price" isRequired />
        <NumberInput source="stockQuantity" isRequired />
        <TextInput source="imageUrl" />
        <BooleanInput source="active" />
        <TextInput source="revision" disabled />
        <ReferenceArrayInput source="categoryIds" reference="categories">
          <AutocompleteArrayInput optionText="name" />
        </ReferenceArrayInput>
      </SimpleForm>
    </Edit>
  )
}
