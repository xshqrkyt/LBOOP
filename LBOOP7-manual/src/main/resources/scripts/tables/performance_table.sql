CREATE TABLE IF NOT EXISTS query_performance (
    id BIGSERIAL PRIMARY KEY,
    branch VARCHAR(50) NOT NULL,
    query VARCHAR(255) NOT NULL,
    execution_time_ms BIGINT NOT NULL
);