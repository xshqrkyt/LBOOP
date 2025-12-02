package com.lab7.dao;

import com.lab7.entity.CompositeFunction;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeFunctionDAO implements DAO<CompositeFunction> {
    private final Connection connection;

    public CompositeFunctionDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Long create(CompositeFunction compFunction) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/insert/insert_composite_function.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/insert/insert_composite_function.sql");

        String insert = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
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

    // Одиночный поиск по id сложной функции
    @Override
    public CompositeFunction findId(Long id) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_composite_function_id.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_composite_function_id.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new CompositeFunction(rs.getLong("id"), rs.getString("name"), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id"));

                return null;
            }
        }
    }

    // Одиночный поиск функций по имени
    public List<CompositeFunction> findName(String name) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_composite_function_name.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_composite_function_name.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        List<CompositeFunction> compFunctions = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    compFunctions.add(new CompositeFunction (rs.getLong("id"), rs.getString("name"), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id")));
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
                    list.add(new CompositeFunction(rs.getLong("id"), rs.getString("name"), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id")));
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
                        result.add(new CompositeFunction(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                                rs.getLong("owner_id")));
            }

            return result;
        }
    }

    // Поиск по иерархии (например, все функции данного владельца)
    public List<CompositeFunction> findOwnerId(Long ownerId) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_composite_function_owner_id.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_composite_function_owner_id.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setLong(1, ownerId);

            List<CompositeFunction> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new CompositeFunction(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                            rs.getLong("owner_id")));
            }

            return list;
        }
    }

    @Override
    public void update(CompositeFunction compFunction) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/update/update_composite_function.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/update/update_composite_function.sql");

        String update = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setString(1, compFunction.getName());
            ps.setLong(2, compFunction.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/delete/delete_composite_function.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/delete/delete_composite_function.sql");

        String delete = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(delete)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}