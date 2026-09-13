import {DeleteButton, UpdateWithConfirmButton, useNotify, useRecordContext} from 'react-admin'

type ProductRecord = {
  id: number
  name: string
  active: boolean
  revision: number
  sku: string
  description: string | null
  price: number
  stockQuantity: number
  imageUrl: string | null
  categoryIds: number[]
}

export function ProductActions() {
  const record = useRecordContext<ProductRecord>()
  const notify = useNotify()

  if (!record) return null

  if (!record.active) {
    return (
      <UpdateWithConfirmButton
        aria-label={`Activate ${record.name}`}
        confirmContent="The product will be available for sale."
        confirmTitle={`Activate ${record.name}?`}
        data={{...record, active: true}}
        label="Activate"
        mutationMode="pessimistic"
        record={record}
        resource="products"
        mutationOptions={{
          onSuccess: () => notify('Product activated', {type: 'success'}),
          onError: () => notify('Unable to activate product', {type: 'error'}),
        }}
      />
    )
  }

  return (
    <DeleteButton
      label="Deactivate"
      confirmTitle={`Deactivate ${record.name}?`}
      confirmContent="The product will no longer be available for sale."
      mutationMode="pessimistic"
      mutationOptions={{
        onSuccess: () => notify('Product deactivated', {type: 'success'}),
        onError: () => notify('Unable to deactivate product', {type: 'error'}),
      }}
    />
  )
}
