CREATE TABLE customer_order (
    id UUID NOT NULL,
    order_number VARCHAR(32) NOT NULL,
    checkout_id UUID NOT NULL,
    customer_id BIGINT NOT NULL,
    total NUMERIC(19,2) NOT NULL,
    placed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_customer_order PRIMARY KEY (id),
    CONSTRAINT uk_customer_order_number UNIQUE (order_number),
    CONSTRAINT uk_customer_order_checkout UNIQUE (customer_id, checkout_id),
    CONSTRAINT chk_customer_order_total CHECK (total >= 0)
);

CREATE TABLE order_item (
    order_id UUID NOT NULL,
    line_number INTEGER NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(50) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price NUMERIC(19,2) NOT NULL,
    quantity INTEGER NOT NULL,
    line_total NUMERIC(19,2) NOT NULL,
    CONSTRAINT pk_order_item PRIMARY KEY (order_id, line_number),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id)
        REFERENCES customer_order (id) ON DELETE CASCADE,
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_item_prices CHECK (unit_price >= 0 AND line_total >= 0)
);

CREATE TABLE order_address (
    order_id UUID NOT NULL,
    address_role VARCHAR(10) NOT NULL,
    recipient_name VARCHAR(200) NOT NULL,
    company_name VARCHAR(200),
    address_line_1 VARCHAR(255) NOT NULL,
    address_line_2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    region VARCHAR(100),
    postal_code VARCHAR(32) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    phone_number VARCHAR(32),
    CONSTRAINT pk_order_address PRIMARY KEY (order_id, address_role),
    CONSTRAINT fk_order_address_order FOREIGN KEY (order_id)
        REFERENCES customer_order (id) ON DELETE CASCADE,
    CONSTRAINT chk_order_address_role CHECK (address_role IN ('SHIPPING','BILLING'))
);

