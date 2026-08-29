CREATE TABLE customer_order (
    id UUID NOT NULL PRIMARY KEY,
    order_number VARCHAR(32) NOT NULL UNIQUE,
    checkout_id UUID NOT NULL,
    customer_id BIGINT NOT NULL,
    shipping_recipient_name VARCHAR(255) NOT NULL,
    shipping_company_name VARCHAR(255),
    shipping_address_line1 VARCHAR(255) NOT NULL,
    shipping_address_line2 VARCHAR(255),
    shipping_city VARCHAR(255) NOT NULL,
    shipping_region VARCHAR(255),
    shipping_postal_code VARCHAR(32) NOT NULL,
    shipping_country_code VARCHAR(2) NOT NULL,
    shipping_phone_number VARCHAR(32),
    billing_recipient_name VARCHAR(255) NOT NULL,
    billing_company_name VARCHAR(255),
    billing_address_line1 VARCHAR(255) NOT NULL,
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(255) NOT NULL,
    billing_region VARCHAR(255),
    billing_postal_code VARCHAR(32) NOT NULL,
    billing_country_code VARCHAR(2) NOT NULL,
    billing_phone_number VARCHAR(32),
    placed_at TIMESTAMP NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    quantity INTEGER NOT NULL
);

CREATE INDEX idx_order_customer_id ON customer_order(customer_id);
CREATE INDEX idx_order_checkout_id ON customer_order(checkout_id);
CREATE INDEX idx_order_item_order_id ON order_item(order_id);

CREATE TABLE checkout_lock (
    customer_id BIGINT NOT NULL,
    checkout_id UUID NOT NULL,
    PRIMARY KEY (customer_id, checkout_id)
);
