package com.lab5.dao;

import com.lab5.entity.Points;

import java.sql.*;
import java.util.*;
import javax.swing.*;

public class PointsDAO implements DAO<Points> {
    private final Connection connection;

    public PointsDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Long create(Points points) throws SQLException {
        String sql = "INSERT INTO points (x_value, y_value, function_id) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Array xArray = connection.createArrayOf("double precision", Arrays.stream(points.getXValues()).toArray(Double[]::new));
            Array yArray = connection.createArrayOf("double precision", Arrays.stream(points.getYValues()).toArray(Double[]::new));

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

    public Points findId(Long id) throws SQLException {
        String sql = "SELECT id, x_value, y_value, function_id FROM points WHERE function_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new Points(rs.getLong("id"), rs.getObject("x_value", Double[].class), rs.getObject("y_value", Double[].class), rs.getLong("function_id"));
            }
        }

        return null;
    }

    // Множественный поиск по нескольким function_id
    public List<Points> findFunctionIds(List<Long> functionIds) throws SQLException {
        if (functionIds == null || functionIds.isEmpty())
            return Collections.emptyList();

        String params = String.join(",", Collections.nCopies(functionIds.size(), "?"));
        String sql = "SELECT * FROM points WHERE function_id IN (" + params + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < functionIds.size(); ++i)
                ps.setLong(i + 1, functionIds.get(i));

            List<Points> results = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    results.add(new Points(rs.getLong("id"), rs.getObject("x_value", Double[].class), rs.getObject("y_value", Double[].class), rs.getLong("function_id")));
            }

            return results;
        }
    }

    // Поиск точек с сортировкой по выбранному полю
    public List<Points> findFunctionIdSorted(Long functionId, String sortByColumn, SortOrder order) throws SQLException {
        // Разрешённые поля для сортировки
        Set<String> allowedSortColumns = Set.of("id", "function_id");
        if (!allowedSortColumns.contains(sortByColumn))
            throw new IllegalArgumentException("Недопустимое поле сортировки");

        String orderDirection = (order == SortOrder.ASCENDING) ? "ASC" : "DESC";

        String sql = "SELECT * FROM points WHERE function_id = ? ORDER BY " + sortByColumn + " " + orderDirection;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, functionId);
            List<Points> results = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    results.add(new Points(rs.getLong("id"), rs.getObject("x_value", Double[].class), rs.getObject("y_value", Double[].class), rs.getLong("function_id")));
            }

            return results;
        }
    }

    // Поиск всех точек пользователя через функции владельца (обход иерархии)
    public List<Points> findUserId(Long userId) throws SQLException {
        String sql = "SELECT p.* FROM points p JOIN function f ON p.function_id = f.id WHERE f.owner_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            List<Points> results = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    results.add(new Points(rs.getLong("id"), rs.getObject("x_value", Double[].class), rs.getObject("y_value", Double[].class), rs.getLong("function_id")));
            }

            return results;
        }
    }

    @Override
    public void update(Points points) throws SQLException {
        String sql = "UPDATE points SET x_value = ?, y_value = ? WHERE function_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            Array xArray = connection.createArrayOf("double precision", Arrays.stream(points.getXValues()).toArray(Double[]::new));
            Array yArray = connection.createArrayOf("double precision", Arrays.stream(points.getYValues()).toArray(Double[]::new));

            ps.setArray(1, xArray);
            ps.setArray(2, yArray);
            ps.setLong(3, points.getFunctionId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM points WHERE function_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}