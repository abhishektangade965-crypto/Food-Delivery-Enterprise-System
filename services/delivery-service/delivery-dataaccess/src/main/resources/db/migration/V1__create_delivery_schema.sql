CREATE TABLE drivers (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,
    vehicle_type VARCHAR(255) NOT NULL,
    vehicle_number VARCHAR(255) NOT NULL,
    license_number VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    approval_status VARCHAR(50) NOT NULL,
    rating NUMERIC(3, 2),
    total_deliveries INT NOT NULL DEFAULT 0,
    acceptance_rate NUMERIC(5, 4) NOT NULL DEFAULT 1.0,
    wallet_balance NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    face_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    latitude NUMERIC(9, 6),
    longitude NUMERIC(9, 6)
);

CREATE TABLE delivery_assignments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    driver_id UUID REFERENCES drivers(id),
    batch_id UUID,
    status VARCHAR(50) NOT NULL,
    pickup_latitude NUMERIC(9, 6) NOT NULL,
    pickup_longitude NUMERIC(9, 6) NOT NULL,
    dropoff_latitude NUMERIC(9, 6) NOT NULL,
    dropoff_longitude NUMERIC(9, 6) NOT NULL,
    estimated_pickup_time TIMESTAMP WITH TIME ZONE,
    estimated_delivery_time TIMESTAMP WITH TIME ZONE,
    actual_pickup_time TIMESTAMP WITH TIME ZONE,
    actual_delivery_time TIMESTAMP WITH TIME ZONE,
    distance_km NUMERIC(6, 2) NOT NULL,
    otp VARCHAR(10),
    otp_verified BOOLEAN NOT NULL DEFAULT FALSE,
    proof_of_delivery_url VARCHAR(1000),
    driver_notes TEXT,
    delivery_fee NUMERIC(10, 2) NOT NULL,
    tip_amount NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE driver_shifts (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL REFERENCES drivers(id),
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE
);

CREATE TABLE driver_locations (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL REFERENCES drivers(id),
    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL
);
