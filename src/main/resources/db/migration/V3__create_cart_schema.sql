CREATE TABLE customer_cart (
    id UUID NOT NULL,
    customer_id BIGINT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_customer_cart PRIMARY KEY (id),
    CONSTRAINT uk_customer_cart_customer UNIQUE (customer_id)
);

CREATE TABLE customer_cart_item (
    cart_id UUID NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT pk_customer_cart_item PRIMARY KEY (cart_id, product_id),
    CONSTRAINT fk_customer_cart_item_cart FOREIGN KEY (cart_id)
        REFERENCES customer_cart (id) ON DELETE CASCADE,
    CONSTRAINT chk_customer_cart_item_quantity CHECK (quantity BETWEEN 1 AND 999)
);

CREATE TABLE consumed_guest_cart (
    guest_cart_id UUID NOT NULL,
    customer_id BIGINT NOT NULL,
    consumed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_consumed_guest_cart PRIMARY KEY (guest_cart_id)
);