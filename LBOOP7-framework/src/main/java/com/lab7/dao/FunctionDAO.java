package com.lab7.dao;

import com.lab7.entity.Function;
import com.lab7.enums.FunctionType;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FunctionDAO implements DAO<Function> {
    private final Connection connection;

    public FunctionDAO(Connection connection) {
        this.connection = connection;
    }

    public Long create(Function function) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/insert/insert_function.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/insert/insert_function.sql");

        String insert = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, function.getName());
            ps.setString(2, function.getType().toString());
            ps.setLong(3, function.getOwnerId());

            ps.executeUpdate();

            try (ResultSet Keys = ps.getGeneratedKeys()) {
                if (Keys.next())
                    return Keys.getLong("id"); // Установить id после вставки
            }
        }

        throw new SQLException("Ошибка при создании пользователя.");
    }

    // Одиночный поиск функции по id
    public Function findId(Long id) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_function_id.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_function_id.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new Function (rs.getLong("id"), rs.getString("name"), FunctionType.valueOf(rs.getString("type")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id"));

                return null;
            }
        }
    }

    // Одиночный поиск функций по имени
    public List<Function> findName(String name) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_function_name.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_function_name.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        List<Function> functions = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    functions.add(new Function (rs.getLong("id"), rs.getString("name"), FunctionType.valueOf(rs.getString("type")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id")));
            }
        }

        return functions;
    }

    // Поиск всех функций пользователя по его id
    public List<Function> findOwnerId(Long ownerId) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_function_owner_id.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_function_owner_id.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        List<Function> functions = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setLong(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    functions.add(new Function(rs.getLong("id"), rs.getString("name"), FunctionType.valueOf(rs.getString("type")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id")));
            }
        }

        return functions;
    }

    // Поиск всех функций пользователя по типу
    public List<Function> findType(FunctionType type) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_function_type.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_function_type.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setString(1, type.toString());

            List<Function> functions = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    functions.add(new Function(rs.getLong("id"), rs.getString("name"), FunctionType.valueOf(rs.getString("type")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id")));
            }

            return functions;
        }
    }

    // Множественный поиск с сортировкой по полю
    public List<Function> findCriteriaSorted(String name, FunctionType type, Long ownerId, String sortByColumn, SortOrder order) throws SQLException {
        Set<String> allowedSortColumns = Set.of("id", "name", "type", "owner_id", "created_at");
        if (sortByColumn != null && !allowedSortColumns.contains(sortByColumn))
            throw new IllegalArgumentException("Недопустимое поле сортировки");

        StringBuilder sql = new StringBuilder("SELECT functions.*, users.username AS owner_name FROM functions JOIN users ON functions.owner_id = users.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null) {
            sql.append(" AND functions.name = ?");
            params.add(name);
        }

        if (type != null) {
            sql.append(" AND functions.type = ?");
            params.add(type.toString());
        }

        if (ownerId != null) {
            sql.append(" AND functions.owner_id = ?");
            params.add(ownerId);
        }

        if (sortByColumn != null) {
            String orderDirection = order == SortOrder.ASCENDING ? "ASC" : "DESC";
            sql.append(" ORDER BY ").append(sortByColumn).append(" ").append(orderDirection);
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            List<Function> functions = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    functions.add(new Function(rs.getLong("id"), rs.getString("name"), FunctionType.valueOf(rs.getString("type")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id")));
                }
            }

            return functions;
        }
    }

    public void update(Function function) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/update/update_function.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/update/update_function.sql");

        String update = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setString(1, function.getName());
            ps.setString(2, function.getType().toString());
            ps.setLong(3, function.getId());

            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/delete/delete_function.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/delete/delete_function.sql");

        String delete = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(delete)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}