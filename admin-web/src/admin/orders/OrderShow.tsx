import {ArrayField, DateField, Datagrid, NumberField, Show, SimpleShowLayout, TextField} from 'react-admin'

export function OrderShow() {
  return (
    <Show>
      <SimpleShowLayout>
        <TextField source="orderNumber" label="Order number" />
        <NumberField source="customerId" label="Customer" />
        <NumberField source="total" options={{style: 'currency', currency: 'EUR'}} />
        <DateField source="placedAt" showTime />
        <ArrayField source="items" label="Items">
          <Datagrid bulkActionButtons={false}>
            <TextField source="sku" />
            <TextField source="productName" label="Product" />
            <NumberField source="quantity" />
            <NumberField source="unitPrice" options={{style: 'currency', currency: 'EUR'}} />
            <NumberField source="lineTotal" options={{style: 'currency', currency: 'EUR'}} />
          </Datagrid>
        </ArrayField>
        <ArrayField source="addresses" label="Addresses">
          <Datagrid bulkActionButtons={false}>
            <TextField source="role" />
            <TextField source="recipientName" />
            <TextField source="addressLine1" />
            <TextField source="city" />
            <TextField source="postalCode" />
            <TextField source="countryCode" />
          </Datagrid>
        </ArrayField>
      </SimpleShowLayout>
    </Show>
  )
}
