package com.lab7.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized helper for creating PostgreSQL connections. Values are picked up from
 * environment variables so the app works both with a local database and a Docker
 * container without code changes.
 *
 * Supported variables (with defaults):
 * DB_HOST (localhost), DB_PORT (5432), DB_NAME (postgres), DB_USER (postgres),
 * DB_PASSWORD (123456789), DB_URL (full JDBC URL overrides host/port/name).
 */
public final class Database {

    private Database() {
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        SQLException lastError = null;

        // 1) Пытаемся подключиться к PostgreSQL (основной вариант)
        try {
            Class.forName("org.postgresql.Driver");

            String url = System.getenv("DB_URL");
            if (url == null || url.isBlank()) {
                String host = getenv("DB_HOST", "localhost");
                String port = getenv("DB_PORT", "5432");
                String name = getenv("DB_NAME", "postgres");
                url = String.format("jdbc:postgresql://%s:%s/%s", host, port, name);
            }

            String user = getenv("DB_USER", "postgres");
            String password = getenv("DB_PASSWORD", "123456789");
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException pgError) {
            lastError = pgError;
        }

        // 2) Фолбэк на файловый H2, чтобы интерфейс и авторизация работали даже без Docker/PG
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection("jdbc:h2:file:./lab7-local-db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        } catch (SQLException h2Error) {
            if (lastError != null) {
                // сохраняем исходное исключение Postgres как причину
                h2Error.addSuppressed(lastError);
            }
            throw h2Error;
        }
    }

    private static String getenv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
