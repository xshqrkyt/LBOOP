-- Добавление пользователя
INSERT INTO users (username, password_hash, email, role) VALUES (?, ?, ?, ?) RETURNING id;

-- Поиск пользователя по id
SELECT * FROM users WHERE id = ?;

-- Поиск пользователя по имени
SELECT * FROM users WHERE username = ?;

-- Обновление пользователя
UPDATE users SET email = ?, password_hash = ?, role = ? WHERE id = ?;

-- Удаление пользователя (с каскадным удалением функций и составных функций)
DELETE FROM users WHERE id = ?;


-- Добавление функции
INSERT INTO function (name, type, owner_id) VALUES (?, ?, ?);

-- Поиск функции по id функции
SELECT function.*, users.username AS owner_name
FROM function
JOIN users ON function.owner_id = users.id
WHERE function.id = ?;

-- Поиск функций по имени функции
SELECT function.*, users.username AS owner_name
FROM function
JOIN users ON function.owner_id = users.id
WHERE function.name = ?;

-- Поиск всех функций пользователя
SELECT function.*, users.username AS owner_name
FROM function
JOIN users ON function.owner_id = users.id
WHERE function.owner_id = ?;

-- Обновление функции (имя и тип)
UPDATE function SET name = ?, type = ? WHERE id = ?;

-- Удаление функции (удаляет связанные точки и ссылки)
DELETE FROM function WHERE id = ?;


-- Вставка новой функции (массивов точек) с function_id
INSERT INTO points (x_value, y_value, function_id) VALUES (?, ?, ?)

-- Получение массивов x и y по function_id
SELECT x_value, y_value
FROM points
WHERE function_id = ?;

-- Получение массивов x и y по id владельца
SELECT p.*
FROM points p
JOIN function f ON p.function_id = f.id
WHERE f.owner_id = ?

-- Обновление массивов точек по function_id
UPDATE points
SET x_value = ?,
    y_value = ?
WHERE function_id = ?;

-- Удаление записи по function_id
DELETE FROM points WHERE function_id = ?;


-- Добавление составной функции
INSERT INTO composite_function (name, owner_id) VALUES (?, ?);

-- Поиск составной функции по id
SELECT composite_function.*, users.username AS owner_name
FROM composite_function
JOIN users ON composite_function.owner_id = users.id
WHERE composite_function.id = ?;

-- Поиск составной функции по имени
SELECT composite_function.*, users.username AS owner_name
FROM composite_function
JOIN users ON composite_function.owner_id = users.id
WHERE composite_function.name = ?;

-- Обновление составной функции (имя)
UPDATE composite_function SET name = ? WHERE id = ?;

-- Удаление составной функции (удаляются ссылки)
DELETE FROM composite_function WHERE id = ?;


-- Добавление ссылки в составную функцию
INSERT INTO composite_function_link (composite_id, function_id, order_index) VALUES (?, ?, ?);

-- Поиск ссылки по id с именами функции и составной функции
SELECT composite_function_link.*, function.name AS function_name, composite_function.name AS composite_name
FROM composite_function_link
JOIN function ON composite_function_link.function_id = function.id
JOIN composite_function ON composite_function_link.composite_id = composite_function.id
WHERE composite_function_link.id = ?;

-- Поиск всех ссылок составной функции с именами функций, упорядоченных
SELECT composite_function_link.*, function.name AS function_name
FROM composite_function_link
JOIN function ON composite_function_link.function_id = function.id
WHERE composite_function_link.composite_id = ?
ORDER BY composite_function_link.order_index;

-- Обновление порядка ссылки
UPDATE composite_function_link SET order_index = ? WHERE id = ?;

-- Удаление ссылки
DELETE FROM composite_function_link WHERE id = ?;