CREATE INDEX ix_customer_order_customer_number
    ON customer_order (customer_id, order_number);

CREATE INDEX ix_customer_order_history
    ON customer_order (customer_id, placed_at DESC, id DESC);
