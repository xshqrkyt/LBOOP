-- crud_queries.sql
-- Examples of parameterized queries for use by the application (PostgreSQL $1, $2 placeholders)

-- USERS
-- Insert user
INSERT INTO users(username, password_hash, email, role) VALUES ($1, $2, $3, $4) RETURNING id;

-- Select user by id
SELECT id, username, password_hash, email, role, created_at FROM users WHERE id = $1;

-- Select user by username
SELECT id, username, password_hash, email, role, created_at FROM users WHERE username = $1;

-- Update user email
UPDATE users SET email = $1 WHERE id = $2;

-- Delete user
DELETE FROM users WHERE id = $1;

-- FUNCTIONS
-- Insert function (returns id)
INSERT INTO functions(owner_id, name, type) VALUES ($1, $2, $3) RETURNING id;

-- Select functions for owner
SELECT id, owner_id, name, type, created_at FROM functions WHERE owner_id = $1 ORDER BY created_at;

-- Update function name
UPDATE functions SET name = $1 WHERE id = $2;

-- Delete function (cascade deletes points)
DELETE FROM functions WHERE id = $1;

-- POINTS
-- Bulk insert points: use transaction and parameterized inserts in application
INSERT INTO points(function_id, x_value, y_value, point_index) VALUES ($1, $2, $3, $4) RETURNING id;

-- Select points for function ordered by index
SELECT id, function_id, x_value, y_value, point_index FROM points WHERE function_id = $1 ORDER BY point_index;

-- Update point y
UPDATE points SET y_value = $1 WHERE id = $2;

-- Delete point
DELETE FROM points WHERE id = $1;
