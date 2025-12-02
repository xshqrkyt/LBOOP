CREATE TABLE IF NOT EXISTS composite_function_link (
    id BIGSERIAL PRIMARY KEY,
    composite_id BIGINT NOT NULL,
    function_id BIGINT NOT NULL,
    order_index INT NOT NULL,

    CONSTRAINT cfk_composite FOREIGN KEY(composite_id) REFERENCES composite_function(id) ON DELETE CASCADE,
    CONSTRAINT cfk_function FOREIGN KEY(function_id) REFERENCES functions(id) ON DELETE CASCADE
);