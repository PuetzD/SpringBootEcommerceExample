# Customer Profile

Customer Profile holds the shopping identity and reusable contact destinations belonging to an authenticated customer. Authentication credentials remain outside this context.

## Language

**Customer**:
The shopping identity associated with one Customer-role Account. A Customer owns its given name, family name, contact Email, and saved Addresses but does not contain authentication credentials.
_Avoid_: Account, buyer, shopper, user

**Customer Created At**:
The immutable instant at which the Customer Profile was created. Administration uses it to count
new Customers within a reporting period.
_Avoid_: Account registration date, first Order date

**Given Name**:
The Customer's personal first name used for identification and customer-facing communication.
_Avoid_: First name field on Account

**Family Name**:
The Customer's personal surname used for identification and customer-facing communication.
_Avoid_: Last name field on Account

**Contact Email**:
The Email a Customer wants to use for order confirmations, fulfillment updates, and support communication. It may differ from the Account's sign-in Email.
_Avoid_: login Email, Account Email

**Address**:
A mutable postal destination saved by a Customer for reuse during checkout. It identifies a recipient and the location where billing correspondence or goods may be directed.
_Avoid_: Order Address, destination record

**Shipping Address**:
An Address selected as the destination for goods.
_Avoid_: Delivery Address

**Billing Address**:
An Address selected for billing correspondence.
_Avoid_: Invoice Address

**Default Shipping Address**:
The optional Address a Customer prefers to preselect for shipping. A Customer has at most one.
_Avoid_: Primary delivery address

**Default Billing Address**:
The optional Address a Customer prefers to preselect for billing. A Customer has at most one.
_Avoid_: Primary invoice address
