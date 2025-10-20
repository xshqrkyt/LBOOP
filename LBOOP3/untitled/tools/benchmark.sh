#!/usr/bin/env bash
# Simple benchmark script: generates SQL for inserting N points and measures time for SELECT
set -e
N=${1:-10000}
DB_URL=jdbc:postgresql://localhost:5432/samsa_db
DB_USER=samsa
DB_PASS=samsa_pass

echo "Generate $N points SQL"
python3 - <<PY
import sys, random
n = int(sys.argv[1])
print("-- SQL to insert function and points")
print("BEGIN;")
print("INSERT INTO functions(owner_id, name, type) VALUES (1, 'bench_func', 'tabulated') RETURNING id;")
for i in range(n):
    x = i+1
    y = random.random()
    print(f"INSERT INTO points(function_id, x_value, y_value, point_index) VALUES (1, {x}, {y}, {i});")
print("COMMIT;")
PY $N

echo "Now measure SELECT time (use psql to run)"
# Note: user should run psql timing or use JDBC program to measure precisely
