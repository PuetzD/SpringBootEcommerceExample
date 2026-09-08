import {AutocompleteArrayInput, BooleanInput, Edit, ReferenceArrayInput, SimpleForm, TextInput} from 'react-admin'
import {ProductVariantsPanel} from './ProductVariantsPanel'

export function ProductEdit() {
  return (
    <Edit mutationMode="pessimistic">
      <>
      <SimpleForm>
        <TextInput source="sku" disabled />
        <TextInput source="name" isRequired />
        <TextInput source="description" multiline />
        <BooleanInput source="active" />
        <TextInput source="revision" disabled />
        <ReferenceArrayInput source="categoryIds" reference="categories">
          <AutocompleteArrayInput optionText="name" />
        </ReferenceArrayInput>
      </SimpleForm>
      <ProductVariantsPanel />
      </>
    </Edit>
  )
}
