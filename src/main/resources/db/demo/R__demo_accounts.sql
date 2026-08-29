INSERT INTO account (id, email, password_hash, role)
VALUES (1, 'customer@shop-happens.com', '{noop}123', 'CUSTOMER'),
       (2, 'admin@shop-happens.com', '{noop}123', 'ADMIN');

INSERT INTO customer (account_id) VALUES (1);

INSERT INTO address (customer_id, recipient_name, address_line_1, city,
                     postal_code, country_code, is_default_shipping, is_default_billing)
SELECT c.id, 'Alex Example', '1 Main Street', 'Testcity', '35037', 'DE', TRUE, TRUE
FROM customer c
WHERE c.account_id = 1;