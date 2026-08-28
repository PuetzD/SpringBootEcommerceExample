-- R_demo_catalog.sql
INSERT INTO category (name, slug)
VALUES ('Electronics', 'electronics'),
       ('Clothing', 'clothing'),
       ('Home & Garden', 'home-garden'),
       ('Sports', 'sports');

INSERT INTO product (sku, name, description, price, stock_quantity, image_url, active)
VALUES ('ELEC-001', 'Wireless Headphones', 'Premium noise-cancelling headphones', 149.99, 25,
        '/images/product-placeholder.svg', TRUE),
       ('ELEC-002', 'Smart Watch', 'Fitness tracking smartwatch', 199.99, 15, '/images/product-placeholder.svg', TRUE),
       ('CLOTH-001', 'Running Shoes', 'Lightweight running shoes', 89.99, 30, '/images/product-placeholder.svg', TRUE),
       ('CLOTH-002', 'Cotton T-Shirt', 'Organic cotton t-shirt', 29.99, 50, '/images/product-placeholder.svg', TRUE),
       ('HOME-001', 'Garden Tool Set', '5-piece garden tool set', 49.99, 20, '/images/product-placeholder.svg', TRUE),
       ('ELEC-003', 'Bluetooth Speaker', 'Portable waterproof speaker', 79.99, 0, '/images/product-placeholder.svg',
        TRUE);

INSERT INTO product_category (product_id, category_id)
VALUES (1, 1),
       (2, 1),
       (3, 4),
       (4, 2),
       (5, 3),
       (6, 1);