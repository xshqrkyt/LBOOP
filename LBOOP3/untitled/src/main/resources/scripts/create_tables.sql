-- create_tables.sql
-- PostgreSQL schema for users, functions, points, composite functions and composite links
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS functions (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'tabulated', 'sqr', 'identity', 'composite', ...
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    UNIQUE(owner_id, name)
);

CREATE TABLE IF NOT EXISTS points (
    id BIGSERIAL PRIMARY KEY,
    function_id BIGINT NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
    x_value DOUBLE PRECISION NOT NULL,
    y_value DOUBLE PRECISION NOT NULL,
    point_index INTEGER NOT NULL -- index to keep order
);

CREATE INDEX IF NOT EXISTS idx_points_function_id ON points(function_id);
CREATE INDEX IF NOT EXISTS idx_points_function_index ON points(function_id, point_index);

CREATE TABLE IF NOT EXISTS composite_functions (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    UNIQUE(owner_id, name)
);

CREATE TABLE IF NOT EXISTS composite_links (
    id BIGSERIAL PRIMARY KEY,
    composite_id BIGINT NOT NULL REFERENCES composite_functions(id) ON DELETE CASCADE,
    function_id BIGINT NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL
);
