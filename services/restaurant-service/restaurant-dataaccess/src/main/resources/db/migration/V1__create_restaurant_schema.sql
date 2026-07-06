CREATE TABLE restaurants (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    approval_status VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL,
    is_featured BOOLEAN NOT NULL,
    rating NUMERIC(3, 2),
    total_ratings INTEGER NOT NULL,
    min_order_amount NUMERIC(10, 2) NOT NULL,
    delivery_fee NUMERIC(10, 2) NOT NULL,
    avg_delivery_time_minutes INTEGER NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    bank_details TEXT,
    surge_multiplier NUMERIC(3, 2) NOT NULL,
    commission_rate NUMERIC(4, 3) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE restaurant_cuisine_types (
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    cuisine_type VARCHAR(100) NOT NULL,
    PRIMARY KEY (restaurant_id, cuisine_type)
);

CREATE TABLE menu_categories (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL,
    available_from TIME,
    available_to TIME,
    image_url VARCHAR(500)
);

CREATE TABLE menu_items (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES menu_categories(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price NUMERIC(10, 2) NOT NULL,
    discounted_price NUMERIC(10, 2),
    food_type VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL,
    is_available BOOLEAN NOT NULL,
    is_featured BOOLEAN NOT NULL,
    calorie_count INTEGER,
    prep_time_minutes INTEGER,
    image_url VARCHAR(500),
    stock_quantity INTEGER,
    track_inventory BOOLEAN NOT NULL,
    rating NUMERIC(3, 2)
);

CREATE TABLE menu_item_tags (
    menu_item_id UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (menu_item_id, tag)
);

CREATE TABLE menu_item_allergens (
    menu_item_id UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    allergen VARCHAR(100) NOT NULL,
    PRIMARY KEY (menu_item_id, allergen)
);

CREATE TABLE menu_item_ingredients (
    menu_item_id UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    inventory_item_id UUID NOT NULL,
    quantity_needed NUMERIC(10, 4) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    PRIMARY KEY (menu_item_id, inventory_item_id)
);

CREATE TABLE restaurant_staff (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    shift_start TIME,
    shift_end TIME,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE inventory_items (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    current_stock NUMERIC(10, 4) NOT NULL,
    reorder_level NUMERIC(10, 4),
    max_stock NUMERIC(10, 4),
    cost_per_unit NUMERIC(10, 2) NOT NULL,
    supplier_info TEXT
);

CREATE INDEX idx_restaurants_slug ON restaurants(slug);
CREATE INDEX idx_restaurants_location ON restaurants(latitude, longitude);
CREATE INDEX idx_menu_categories_restaurant ON menu_categories(restaurant_id);
CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_inventory_items_restaurant ON inventory_items(restaurant_id);
CREATE INDEX idx_restaurant_staff_restaurant ON restaurant_staff(restaurant_id);
