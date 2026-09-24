import {
    DateField,
    NumberField,
    RecordContextProvider,
    SimpleShowLayout,
    TextField,
    useGetOne,
    useRecordContext
} from 'react-admin'
import type {Order, OrderConfirmationStatusRecord} from '../../api/types'

export function OrderConfirmationStatusPanel() {
    const order = useRecordContext<Order>()
    const orderNumber = order?.orderNumber
    const {data, isPending} = useGetOne<OrderConfirmationStatusRecord>(
        'orderConfirmationStatus',
        {id: orderNumber ?? ''},
        {enabled: Boolean(orderNumber)},
    )
    if (isPending || !data) {
        return null
    }

    return (
        <RecordContextProvider value={data}>
            <SimpleShowLayout>
                <TextField source="status" label="Status" />
                <NumberField source="failedAttempts" label="Failed attempts" />
                <TextField source="lastError" label="Last error" />
                <DateField source="retryAt" label="Retry at" showTime />
                <DateField source="sentAt" label="Sent at" showTime />
            </SimpleShowLayout>
        </RecordContextProvider>
    );
}
