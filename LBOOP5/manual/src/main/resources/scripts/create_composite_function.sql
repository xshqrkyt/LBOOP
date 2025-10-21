CREATE TABLE composite_function (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    owner_id BIGINT,
    
    CONSTRAINT fk_owner FOREIGN KEY(owner_id) REFERENCES user(id)
);