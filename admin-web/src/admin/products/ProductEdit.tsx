import {AutocompleteArrayInput, BooleanInput, Edit, ReferenceArrayInput, SimpleForm, TextInput, required} from 'react-admin'
import {ProductVariantsPanel} from './ProductVariantsPanel'

export function ProductEdit() {
  return (
    <Edit mutationMode="pessimistic">
      <>
      <SimpleForm>
        <TextInput source="sku" disabled />
        <TextInput source="name" validate={required('Required field')} />
        <TextInput source="description" multiline />
        <BooleanInput source="active" />
        <TextInput source="revision" disabled />
        <ReferenceArrayInput source="categoryIds" reference="categoryOptions">
          <AutocompleteArrayInput optionText="name" />
        </ReferenceArrayInput>
      </SimpleForm>
      <ProductVariantsPanel />
      </>
    </Edit>
  )
}
