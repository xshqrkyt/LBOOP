CREATE TABLE point (
    id BIGINT PRIMARY KEY,
    x_value DOUBLE PRECISION NOT NULL,
    y_value DOUBLE PRECISION NOT NULL,
    function_id BIGINT,

    CONSTRAINT fk_function FOREIGN KEY(function_id) REFERENCES function(id)
);