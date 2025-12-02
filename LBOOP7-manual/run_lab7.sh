#!/usr/bin/env bash
set -euo pipefail

# Quick bootstrap for Lab 7 webapp.
# Usage: DB_USER=postgres DB_PASSWORD=123456789 ./run_lab7.sh

DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-postgres}
DB_USER=${DB_USER:-postgres}
DB_PASSWORD=${DB_PASSWORD:-123456789}
START_SERVER=${START_SERVER:-1}
# USE_H2=1 — пропустить psql и полагаться на встроенную файловую H2 (lab7-local-db.mv.db)
USE_H2=${USE_H2:-0}
PROJECT_ROOT=$(cd "$(dirname "$0")" && pwd)
TABLES_DIR="$PROJECT_ROOT/src/main/resources/scripts/tables"

command -v mvn >/dev/null 2>&1 || { echo "mvn не найден. Установите Maven 3+ с JDK 17."; exit 1; }

if [[ "$USE_H2" -eq 0 ]]; then
  command -v psql >/dev/null 2>&1 || { echo "psql не найден. Установите PostgreSQL client или запустите с USE_H2=1 для локальной базы."; exit 1; }
  export PGPASSWORD="$DB_PASSWORD"
fi

run_sql() {
  local file="$1"
  echo "→ Применяем $file"
  psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 -f "$file"
}

if [[ "$USE_H2" -eq 0 ]]; then
  echo "=== Создание таблиц в $DB_NAME на $DB_HOST:$DB_PORT ==="
  run_sql "$TABLES_DIR/users_table.sql"
  run_sql "$TABLES_DIR/function_table.sql"
  run_sql "$TABLES_DIR/points_table.sql"
  run_sql "$TABLES_DIR/composite_function_table.sql"
  run_sql "$TABLES_DIR/composite_function_link_table.sql"
  run_sql "$TABLES_DIR/performance_table.sql"
  run_sql "$TABLES_DIR/sorting_performance_table.sql"
else
  echo "=== Пропускаем psql (USE_H2=1). При старте приложения таблицы создаст DatabaseInitializer во встроенной H2: ./lab7-local-db.mv.db ==="
fi

echo "=== Сборка WAR-файла ==="
mvn -pl LBOOP7-manual -am package -DskipTests

if [[ "$START_SERVER" -eq 1 ]]; then
  echo "=== Запуск сервера (Ctrl+C для остановки) ==="
  mvn -pl LBOOP7-manual -am spring-boot:run -DskipTests
else
  echo "Сервер не запускался (START_SERVER=$START_SERVER). Выполните:\n  mvn -pl LBOOP7-manual -am spring-boot:run -DskipTests"
fi
