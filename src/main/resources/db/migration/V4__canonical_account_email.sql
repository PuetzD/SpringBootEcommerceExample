UPDATE account SET email = lower(trim(email));
ALTER TABLE account DROP CONSTRAINT uk_account_email;
ALTER TABLE account ADD CONSTRAINT chk_account_email_canonical
    CHECK (email = lower(trim(email)));
CREATE UNIQUE INDEX uk_account_email_canonical ON account (lower(email));