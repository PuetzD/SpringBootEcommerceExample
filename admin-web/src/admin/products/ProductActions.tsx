import {DeleteButton, useNotify, useRecordContext, useUpdate} from 'react-admin'

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
  const [update, {isPending}] = useUpdate()
  const notify = useNotify()

  if (!record) return null

  if (!record.active) {
    return (
      <button
        type="button"
        aria-label={`Activate ${record.name}`}
        disabled={isPending}
        onClick={() =>
          update(
            'products',
            {id: record.id, data: {...record, active: true}, previousData: record},
            {
              onSuccess: () => notify('Product activated', {type: 'success'}),
              onError: () => notify('Unable to activate product', {type: 'error'}),
            },
          )
        }
      >
        Activate
      </button>
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
