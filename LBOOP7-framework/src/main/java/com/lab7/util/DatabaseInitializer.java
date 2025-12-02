package com.lab7.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Idempotently applies the SQL scripts from resources/scripts/tables on startup so the
 * application can run even if the user forgot to create tables manually. Intended for
 * lightweight bootstrapping; failures are logged but don't crash the app.
 */
public final class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private static final List<String> SCRIPT_PATHS = List.of(
            "/scripts/tables/users_table.sql",
            "/scripts/tables/function_table.sql",
            "/scripts/tables/points_table.sql",
            "/scripts/tables/composite_function_table.sql",
            "/scripts/tables/composite_function_link_table.sql",
            "/scripts/tables/performance_table.sql",
            "/scripts/tables/sorting_performance_table.sql"
    );

    public static void ensureReady() {
        try (Connection connection = Database.getConnection()) {
            new DatabaseInitializer().initialize(connection);
        }
        catch (Exception e) {
            logger.error("Failed to initialize schema", e);
        }
    }

    public void initialize(Connection connection) {
        SCRIPT_PATHS.forEach(path -> applyFromResource(path, connection));
    }

    private String readSql(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private void applyFromResource(String path, Connection connection) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                logger.warn("Init script not found: {}", path);
                return;
            }

            String sql = readSql(stream);
            try (Statement stmt = connection.createStatement()) {
                for (String statement : sql.split(";")) {
                    String trimmed = statement.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    try {
                        stmt.execute(trimmed);
                    }
                    catch (SQLException e) {
                        // Ignore "already exists" style errors so scripts are idempotent
                        logger.debug("Skipping statement from {}: {} (reason: {})", path, trimmed, e.getMessage());
                    }
                }
            }
        }
        catch (IOException e) {
            logger.error("Failed to read init script {}", path, e);
        }
        catch (SQLException e) {
            logger.error("Failed to apply init script {}", path, e);
        }
    }
}
