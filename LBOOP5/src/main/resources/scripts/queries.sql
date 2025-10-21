-- User CRUD
INSERT INTO "user" (username, password_hash, email, role) VALUES (?, ?, ?, ?::user_role);
SELECT * FROM "user" WHERE id = ?;
SELECT * FROM "user" WHERE username = ?;
UPDATE "user" SET email = ? WHERE id = ?;
DELETE FROM "user" WHERE id = ?;

-- Function CRUD
INSERT INTO function (name, type, owner_id) VALUES (?, ?, ?) RETURNING id;
SELECT * FROM function WHERE id = ?;
SELECT * FROM function WHERE owner_id = ?;
UPDATE function SET name = ? WHERE id = ?;
DELETE FROM function WHERE id = ?;

-- Point CRUD
INSERT INTO point (x_value, y_value, function_id) VALUES (?, ?, ?);
SELECT * FROM point WHERE function_id = ?;
SELECT * FROM point WHERE id = ?;
UPDATE point SET x_value = ?, y_value = ? WHERE id = ?;
DELETE FROM point WHERE function_id = ?;

-- Composite Function CRUD
INSERT INTO composite_function (name, owner_id) VALUES (?, ?) RETURNING id;
INSERT INTO composite_function_link (composite_id, function_id, order_index) VALUES (?, ?, ?);
SELECT cf.*, u.username as owner_name FROM composite_function cf 
JOIN "user" u ON cf.owner_id = u.id WHERE cf.id = ?;
SELECT f.* FROM function f 
JOIN composite_function_link cfl ON f.id = cfl.function_id 
WHERE cfl.composite_id = ? ORDER BY cfl.order_index;
