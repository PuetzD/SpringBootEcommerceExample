import {ArrayField, BooleanField, Datagrid, DateField, FunctionField, NumberField, Show, SimpleShowLayout, TextField} from 'react-admin'
import {Link} from 'react-router-dom'
import type {CustomerOrder} from '../../api/types'

export function CustomerShow({id}: {id?: number}) {
  return (
    <Show {...(id === undefined ? {} : {id})}>
      <SimpleShowLayout>
        <NumberField source="id" label="Customer ID" />
        <TextField source="givenName" label="First name" />
        <TextField source="familyName" label="Last name" />
        <TextField source="contactEmail" label="Email" />
        <NumberField source="accountId" label="Account ID" />
        <ArrayField source="addresses" label="Addresses">
          <Datagrid bulkActionButtons={false}>
            <TextField source="recipientName" label="Recipient" />
            <TextField source="addressLine1" />
            <TextField source="addressLine2" />
            <TextField source="city" />
            <TextField source="region" />
            <TextField source="postalCode" />
            <TextField source="countryCode" />
            <BooleanField source="defaultShipping" label="Default shipping" />
            <BooleanField source="defaultBilling" label="Default billing" />
          </Datagrid>
        </ArrayField>
        <ArrayField source="orders" label="Orders">
          <Datagrid bulkActionButtons={false}>
            <FunctionField
              source="orderNumber"
              label="Order number"
              render={(order: CustomerOrder) => (
                <Link to={order.orderUrl}>{order.orderNumber}</Link>
              )}
            />
            <NumberField source="total" options={{style: 'currency', currency: 'EUR'}} />
            <DateField source="placedAt" showTime />
          </Datagrid>
        </ArrayField>
      </SimpleShowLayout>
    </Show>
  )
}
