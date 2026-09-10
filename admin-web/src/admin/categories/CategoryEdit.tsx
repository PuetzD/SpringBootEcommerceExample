import {
  Edit,
  required,
  SaveButton,
  SimpleForm,
  TextInput,
  Toolbar,
  useNotify,
  useRefresh,
} from 'react-admin'

function conflictCode(error: unknown): string | undefined {
  if (typeof error !== 'object' || error === null) return undefined
  const code = (error as {code?: unknown}).code
  return typeof code === 'string' ? code : undefined
}

function CategoryEditToolbar() {
  return (
    <Toolbar>
      <SaveButton />
    </Toolbar>
  )
}

export function CategoryEdit() {
  const notify = useNotify()
  const refresh = useRefresh()

  return (
    <Edit
      mutationMode="pessimistic"
      mutationOptions={{
        onError: (error) => {
          if (conflictCode(error) === 'catalog.category.stale') {
            notify('Category changed on the server. The form was refreshed; choose the action again.', {
              type: 'error',
            })
            refresh()
            return
          }

          notify('Unable to rename category', {type: 'error'})
        },
      }}>
      <SimpleForm toolbar={<CategoryEditToolbar />}>
        <TextInput source="name" validate={required('Required field')} />
        <TextInput source="slug" disabled />
        <TextInput source="productCount" disabled />
        <TextInput source="revision" disabled />
      </SimpleForm>
    </Edit>
  )
}
