
-- User CRUD
INSERT INTO "user" (username, password_hash, email, role) VALUES (?, ?, ?, ?::user_role);
SELECT * FROM "user" WHERE id = ?;
UPDATE "user" SET email = ? WHERE id = ?;
DELETE FROM "user" WHERE id = ?;

-- Function CRUD
INSERT INTO function (name, type, owner_id) VALUES (?, ?, ?) RETURNING id;
SELECT * FROM function WHERE id = ?;
UPDATE function SET name = ? WHERE id = ?;
DELETE FROM function WHERE id = ?;

-- Point CRUD
INSERT INTO point (x_value, y_value, function_id) VALUES (?, ?, ?);
SELECT * FROM point WHERE function_id = ?;
UPDATE point SET x_value = ?, y_value = ? WHERE id = ?;
DELETE FROM point WHERE function_id = ?;
