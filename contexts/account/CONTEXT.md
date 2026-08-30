# Identity and Access

Identity and Access establishes who can sign in and which broad application area they may access. It deliberately excludes shopping profile and purchase data.

## Language

**Account**:
An authentication identity identified by its canonical Email and assigned exactly one Role. An Account may be enabled or disabled.
_Avoid_: User, login, customer Account

**Email**:
The canonical, case-insensitive sign-in identifier for an Account. Leading and trailing whitespace and letter case do not distinguish Accounts.
At the security adapter boundary, framework surface terms (Spring Security's `UserDetails`, `username`) appear without denoting a domain concept; the model always calls it Email.
_Avoid_: Username, login name

**Role**:
The single broad access classification assigned to an Account: Customer or Administrator.
_Avoid_: Permission, authority, multiple roles

**Customer Role**:
The Role for an Account whose owner may use customer-only shopping capabilities.
_Avoid_: Shopper role, user role

**Administrator Role**:
The Role for an Account whose owner may use administration capabilities.
_Avoid_: Admin user, staff role

**Registration**:
The creation of a new Customer-role Account and its corresponding Customer identity.
_Avoid_: Sign-in, enrollment
