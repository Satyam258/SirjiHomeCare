CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(140) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'ADMIN')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE site_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key VARCHAR(120) NOT NULL UNIQUE,
    setting_value VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(140) NOT NULL,
    address TEXT NOT NULL,
    latitude NUMERIC(9, 6) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude NUMERIC(9, 6) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    radius_km NUMERIC(8, 2) NOT NULL CHECK (radius_km > 0),
    priority INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_service_branches_active_priority ON service_branches(active, priority DESC);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL REFERENCES categories(id),
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    unit VARCHAR(80) NOT NULL,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_products_category_active ON products(category_id, active);

CREATE TABLE customer_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(20) NOT NULL UNIQUE,
    customer_name VARCHAR(160) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,
    delivery_address TEXT NOT NULL,
    latitude NUMERIC(9, 6) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude NUMERIC(9, 6) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    branch_id UUID NOT NULL REFERENCES service_branches(id),
    status VARCHAR(24) NOT NULL CHECK (status IN ('PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'FULFILLED')),
    bulk_order BOOLEAN NOT NULL DEFAULT FALSE,
    reviewed_by UUID REFERENCES admin_users(id),
    admin_notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_customer_orders_status_created ON customer_orders(status, created_at DESC);
CREATE INDEX idx_customer_orders_number_phone ON customer_orders(order_number, customer_phone);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES customer_orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_order_items_order ON order_items(order_id);

CREATE TABLE feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_name VARCHAR(160) NOT NULL,
    email VARCHAR(254),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    message TEXT NOT NULL,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_feedback_approved_created ON feedback(approved, created_at DESC);

INSERT INTO categories (id, name, slug) VALUES
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'Personal care', 'personal-care'),
    ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'First aid', 'first-aid'),
    ('cccccccc-cccc-4ccc-8ccc-cccccccccccc', 'Wellness', 'wellness'),
    ('dddddddd-dddd-4ddd-8ddd-dddddddddddd', 'Elder care', 'elder-care'),
    ('eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', 'Home hygiene', 'home-hygiene');

INSERT INTO products (id, category_id, name, slug, description, price, unit, image_url) VALUES
    ('11111111-1111-4111-8111-111111111111', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'Daily Care Essentials', 'daily-care-essentials', 'Gentle daily-use essentials selected for comfort and dependable care.', 249, 'care kit', 'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?auto=format&fit=crop&w=720&q=85'),
    ('22222222-2222-4222-8222-222222222222', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'Home First Aid Kit', 'home-first-aid-kit', 'A practical assortment of first-aid basics for a well-prepared household.', 499, 'kit', 'https://images.unsplash.com/photo-1603398938378-e54eab446dde?auto=format&fit=crop&w=720&q=85'),
    ('33333333-3333-4333-8333-333333333333', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'Nourish Hand Cream', 'nourish-hand-cream', 'A rich, non-greasy moisturizer for hands that need a little extra care.', 189, '100 ml', 'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?auto=format&fit=crop&w=720&q=85'),
    ('44444444-4444-4444-8444-444444444444', 'cccccccc-cccc-4ccc-8ccc-cccccccccccc', 'Family Wellness Pack', 'family-wellness-pack', 'A considered bundle of home wellness products for the whole family.', 799, 'monthly pack', 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=720&q=85'),
    ('55555555-5555-4555-8555-555555555555', 'dddddddd-dddd-4ddd-8ddd-dddddddddddd', 'Comfort Mobility Support', 'comfort-mobility-support', 'Useful at-home support items selected with comfort and independence in mind.', 649, 'support set', 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=720&q=85'),
    ('66666666-6666-4666-8666-666666666666', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', 'Clean Living Bundle', 'clean-living-bundle', 'Reliable home hygiene supplies for the spaces your family uses every day.', 359, 'bundle', 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=720&q=85');

INSERT INTO service_branches (id, name, address, latitude, longitude, radius_km, priority) VALUES
    ('88888888-8888-4888-8888-888888888888', 'Demo service branch', 'Replace with your real service branch address before launch', 12.971600, 77.594600, 25.00, 1);

INSERT INTO site_settings (setting_key, setting_value) VALUES
    ('bulk.quantity.threshold', '50');
