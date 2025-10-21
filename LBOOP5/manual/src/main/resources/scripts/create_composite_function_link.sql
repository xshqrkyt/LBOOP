CREATE TABLE composite_function_link (
    id BIGINT PRIMARY KEY,
    composite_id BIGINT NOT NULL,
    function_id BIGINT NOT NULL,
    order_index INT NOT NULL,

    CONSTRAINT fk_composite FOREIGN KEY(composite_id) REFERENCES composite_function(id),
    CONSTRAINT fk_function FOREIGN KEY(function_id) REFERENCES function(id)
);