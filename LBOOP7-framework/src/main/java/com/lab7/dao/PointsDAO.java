package com.lab7.dao;

import com.lab7.entity.Points;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PointsDAO implements DAO<Points> {
    private final Connection connection;

    public PointsDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Long create(Points points) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/insert/insert_points.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/insert/insert_points.sql");

        String insert = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            String arrayType = getArrayType(connection);
            Array xArray = connection.createArrayOf(arrayType, Arrays.stream(points.getXValues()).toArray(Double[]::new));
            Array yArray = connection.createArrayOf(arrayType, Arrays.stream(points.getYValues()).toArray(Double[]::new));

            ps.setArray(1, xArray);
            ps.setArray(2, yArray);
            ps.setLong(3, points.getFunctionId());

            ps.executeUpdate();

            try (ResultSet Keys = ps.getGeneratedKeys()) {
                if (Keys.next())
                    return Keys.getLong("id"); // Установить id после вставки
            }
        }

        throw new SQLException("Ошибка при создании точек.");
    }

    // Поиск по id функции
    public Points findId(Long id) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_points_id.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_points_id.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();
                    if (dbProductName.contains("postgresql")) {
                        Array xArray = rs.getArray("x_value");
                        Double[] xValues = (Double[]) xArray.getArray();

                        Array yArray = rs.getArray("y_value");
                        Double[] yValues = (Double[]) yArray.getArray();

                        return new Points(rs.getLong("id"), xValues, yValues, rs.getLong("function_id"));
                    }

                    else if (dbProductName.contains("h2"))
                        return new Points(rs.getLong("id"), rs.getObject("x_value", Double[].class), rs.getObject("y_value", Double[].class), rs.getLong("function_id"));

                    else
                        throw new SQLException("Unsupported DB: " + dbProductName);
                }
            }
        }

        return null;
    }

    // Поиск точек с сортировкой по выбранному полю
    public List<Points> findFunctionIdSorted(Long functionId, String sortByColumn, SortOrder order) throws SQLException {
        // Разрешённые поля для сортировки
        Set<String> allowedSortColumns = Set.of("id", "function_id", "created_at");
        if (!allowedSortColumns.contains(sortByColumn))
            throw new IllegalArgumentException("Недопустимое поле сортировки");

        String orderDirection = (order == SortOrder.ASCENDING) ? "ASC" : "DESC";

        String sql = "SELECT * FROM points WHERE function_id = ? ORDER BY " + sortByColumn + " " + orderDirection;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, functionId);
            List<Points> results = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                String dbProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();
                while (rs.next()) {
                    if (dbProductName.contains("postgresql")) {
                        Array xArray = rs.getArray("x_value");
                        Double[] xValues = (Double[]) xArray.getArray();

                        Array yArray = rs.getArray("y_value");
                        Double[] yValues = (Double[]) yArray.getArray();

                        results.add(new Points(rs.getLong("id"), xValues, yValues, rs.getLong("function_id")));

                    }

                    else if (dbProductName.contains("h2"))
                        results.add(new Points(rs.getLong("id"), rs.getObject("x_value", Double[].class), rs.getObject("y_value", Double[].class), rs.getLong("function_id")));

                    else
                        throw new SQLException("Unsupported DB: " + dbProductName);
                }
            }

            return results;
        }
    }

    // Поиск точек по id пользователя с сортировкой по выбранному столбцу
    public List<Points> findOwnerIdSorted(Long ownerId, String sortByColumn, SortOrder order) throws SQLException {
        // Разрешённые поля для сортировки
        Set<String> allowedSortColumns = Set.of("id", "function_id", "created_at"); // добавьте нужные поля
        if (!allowedSortColumns.contains(sortByColumn))
            throw new IllegalArgumentException("Недопустимое поле сортировки");

        String orderDirection = (order == SortOrder.ASCENDING) ? "ASC" : "DESC";

        // SQL с join между points и functions по owner_id функции
        String sql = "SELECT p.* FROM points p JOIN functions f ON p.function_id = f.id WHERE f.owner_id = ? ORDER BY " + sortByColumn + " " + orderDirection;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            List<Points> results = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                String dbProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();
                while (rs.next()) {
                    if (dbProductName.contains("postgresql")) {
                        Array xArray = rs.getArray("x_value");
                        Double[] xValues = (Double[]) xArray.getArray();

                        Array yArray = rs.getArray("y_value");
                        Double[] yValues = (Double[]) yArray.getArray();

                        results.add(new Points(rs.getLong("id"), xValues, yValues, rs.getLong("function_id")));
                    }

                    else if (dbProductName.contains("h2"))
                        results.add(new Points(rs.getLong("id"), rs.getObject("x_value", Double[].class), rs.getObject("y_value", Double[].class), rs.getLong("function_id")));

                    else
                        throw new SQLException("Unsupported DB: " + dbProductName);
                }
            }

            return results;
        }
    }

    // Поиск всех точек пользователя через функции владельца (обход иерархии)
    public List<Points> findUserId(Long userId) throws SQLException, IOException {
        String find = new String(Files.readAllBytes(Paths.get("src/main/resources/scripts/find/find_points_user_id.sql")));

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setLong(1, userId);
            List<Points> results = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                String dbProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();
                while (rs.next()) {
                    if (dbProductName.contains("postgresql")) {
                        Array xArray = rs.getArray("x_value");
                        Double[] xValues = (Double[]) xArray.getArray();

                        Array yArray = rs.getArray("y_value");
                        Double[] yValues = (Double[]) yArray.getArray();

                        results.add(new Points(rs.getLong("id"), xValues, yValues, rs.getLong("function_id")));
                    }

                    else if (dbProductName.contains("h2"))
                        results.add(new Points(rs.getLong("id"), rs.getObject("x_value", Double[].class), rs.getObject("y_value", Double[].class), rs.getLong("function_id")));

                    else
                        throw new SQLException("Unsupported DB: " + dbProductName);
                }
            }

            return results;
        }
    }

    @Override
    public void update(Points points) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/update/update_points.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/update/update_points.sql");

        String update = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(update)) {
            String arrayType = getArrayType(connection);

            Array xArray = connection.createArrayOf(arrayType, points.getXValues());
            Array yArray = connection.createArrayOf(arrayType, points.getYValues());

            ps.setArray(1, xArray);
            ps.setArray(2, yArray);
            ps.setLong(3, points.getFunctionId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/delete/delete_points.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/delete/delete_points.sql");

        String delete = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(delete)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private String getArrayType(Connection connection) throws SQLException {
        String dbProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();
        if (dbProductName.contains("postgresql"))
            return "float8";  // PostgreSQL

        else if (dbProductName.contains("h2"))
            return "double precision";

        else
            throw new SQLException("Unsupported DB: " + dbProductName);
    }
}