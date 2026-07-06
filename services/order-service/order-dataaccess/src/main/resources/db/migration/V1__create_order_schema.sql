CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    tracking_id UUID NOT NULL UNIQUE,
    price NUMERIC(10, 2) NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    failure_messages VARCHAR(1000),
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL
);

CREATE TABLE order_items (
    id BIGINT NOT NULL,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity INT NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    sub_total NUMERIC(10, 2) NOT NULL,
    PRIMARY KEY (id, order_id)
);

CREATE TABLE order_restaurant_m_view (
    id UUID PRIMARY KEY,
    active BOOLEAN NOT NULL
);

INSERT INTO order_restaurant_m_view(id, active) VALUES ('d290f1d6-53b7-4c0a-ae8d-ec44a864cfd8', TRUE);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_tracking_id ON orders(tracking_id);
