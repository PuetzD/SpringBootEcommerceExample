INSERT INTO account (id, email, password_hash, role)
VALUES (1, 'customer@shop-happens.com', '{noop}123', 'CUSTOMER'),
       (2, 'admin@shop-happens.com', '{noop}123', 'ADMIN');

INSERT INTO customer (account_id) VALUES (1);