package com.lab5.dao;

import com.lab5.entity.CompositeFunction;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public class CompositeFunctionDAO implements DAO<CompositeFunction> {
    private final Connection connection;

    public CompositeFunctionDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Long create(CompositeFunction compFunction) throws SQLException {
        String sql = "INSERT INTO composite_function (name, owner_id) VALUES (?, ?);";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, compFunction.getName());
            ps.setLong(2, compFunction.getOwnerId());

            ps.executeUpdate();

            try (ResultSet Keys = ps.getGeneratedKeys()) {
                if (Keys.next())
                    return Keys.getLong("id");
            }
        }

        throw new SQLException("Ошибка при создании сложной функции.");
    }

    // Одиночный поиск по id
    @Override
    public CompositeFunction findId(Long id) throws SQLException {
        String sql = "SELECT composite_function.*, users.username AS owner_name FROM composite_function JOIN users ON composite_function.owner_id = users.id WHERE composite_function.id = ?;";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new CompositeFunction(rs.getLong("id"), rs.getString("name"), rs.getLong("owner_id"), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);

                return null;
            }
        }
    }

    // Одиночный поиск функций по имени
    public List<CompositeFunction> findName(String name) throws SQLException {
        String sql = "SELECT composite_function.*, users.username AS owner_name FROM composite_function JOIN users ON composite_function.owner_id = users.id WHERE composite_function.name = ?";
        List<CompositeFunction> compFunctions = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    compFunctions.add(new CompositeFunction (rs.getLong("id"), rs.getString("name"), rs.getLong("owner_id"), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null));
            }
        }

        return compFunctions;
    }

    // Множественный поиск по списку имён
    public List<CompositeFunction> findNames(List<String> names) throws SQLException {
        if (names == null || names.isEmpty())
            return new ArrayList<>();

        String placeholders = String.join(",", names.stream().map(n -> "?").toArray(String[]::new));
        String sql = "SELECT composite_function.*, users.username AS owner_name FROM composite_function JOIN users ON composite_function.owner_id = users.id WHERE composite_function.name IN (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < names.size(); ++i)
                ps.setString(i + 1, names.get(i));

            List<CompositeFunction> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new CompositeFunction(rs.getLong("id"), rs.getString("name"), rs.getLong("owner_id"), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null));
            }

            return list;
        }
    }

    // Множественный поиск по списку имён с сортировкой
    public List<CompositeFunction> findAllSorted(List<String> names, String sortBy, SortOrder sortOrder) throws SQLException {
        if (names == null || names.isEmpty())
            return new ArrayList<>();

        List<String> allowedFields = List.of("name", "created_at", "owner_id");
        if (sortBy == null || !allowedFields.contains(sortBy.toLowerCase()))
            throw new IllegalArgumentException("Недопустимое поле сортировки: " + sortBy);

        String order = (sortOrder == SortOrder.DESCENDING) ? "DESC" : "ASC";

        String placeholders = String.join(",", Collections.nCopies(names.size(), "?"));

        String sql = "SELECT composite_function.*, users.username AS owner_name " +
                "FROM composite_function " +
                "JOIN users ON composite_function.owner_id = users.id " +
                "WHERE composite_function.name IN (" + placeholders + ") " +
                "ORDER BY composite_function." + sortBy + " " + order;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < names.size(); ++i)
                ps.setString(i + 1, names.get(i));

            List<CompositeFunction> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new CompositeFunction(rs.getLong("id"), rs.getString("name"), rs.getLong("owner_id"), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null));
            }

            return result;
        }
    }

    // Поиск по иерархии (например, все функции данного владельца)
    public List<CompositeFunction> findOwnerId(Long ownerId) throws SQLException {
        String sql = "SELECT composite_function.*, users.username AS owner_name FROM composite_function JOIN users ON composite_function.owner_id = users.id WHERE composite_function.owner_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerId);

            List<CompositeFunction> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new CompositeFunction(rs.getLong("id"), rs.getString("name"), rs.getLong("owner_id"), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null));
            }

            return list;
        }
    }

    @Override
    public void update(CompositeFunction compFunction) throws SQLException {
        String sql = "UPDATE composite_function SET name = ? WHERE id = ?;";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, compFunction.getName());
            ps.setLong(2, compFunction.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM composite_function WHERE id = ?;";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}