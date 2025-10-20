CREATE TABLE function (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('tabulated', 'sqr', 'identity', 'composite', 'constant', 'method_newton', 'de_boor', 'sin')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    owner_id BIGINT,

    CONSTRAINT fk_owner FOREIGN KEY(owner_id) REFERENCES user(id)
);