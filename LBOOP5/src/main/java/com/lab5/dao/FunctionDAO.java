package com.lab5.dao;

import com.lab5.enums.FunctionType;
import com.lab5.entity.Function;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public class FunctionDAO implements DAO<Function> {
    private final Connection connection;

    public FunctionDAO(Connection connection) {
        this.connection = connection;
    }

    public Long create(Function function) throws SQLException {
        String sql = "INSERT INTO function (name, type, owner_id) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
    public Function findId(Long id) throws SQLException {
        String sql = "SELECT function.*, users.username AS owner_name FROM function JOIN users ON function.owner_id = users.id WHERE function.id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new Function (rs.getLong("id"), rs.getString("name"), FunctionType.valueOf(rs.getString("type")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id"));

                return null;
            }
        }
    }

    // Одиночный поиск функций по имени
    public List<Function> findName(String name) throws SQLException {
        String sql = "SELECT function.*, users.username AS owner_name FROM function JOIN users ON function.owner_id = users.id WHERE function.name = ?;";
        List<Function> functions = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    functions.add(new Function (rs.getLong("id"), rs.getString("name"), FunctionType.valueOf(rs.getString("type")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id")));
            }
        }

        return functions;
    }

    // Поиск всех функций пользователя по его id
    public List<Function> findOwnerId(Long ownerId) throws SQLException {
        String sql = "SELECT function.*, users.username AS owner_name FROM function JOIN users ON function.owner_id = users.id WHERE function.owner_id = ?";

        List<Function> functions = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    functions.add(new Function(rs.getLong("id"), rs.getString("name"), FunctionType.valueOf(rs.getString("type")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, rs.getLong("owner_id")));
            }
        }

        return functions;
    }

    // Множественный поиск с сортировкой по полю
    public List<Function> findCriteriaSorted(String name, FunctionType type, Long ownerId, String sortByColumn, SortOrder order) throws SQLException {
        Set<String> allowedSortColumns = Set.of("id", "name", "type", "owner_id", "created_at");
        if (sortByColumn != null && !allowedSortColumns.contains(sortByColumn))
            throw new IllegalArgumentException("Недопустимое поле сортировки");

        StringBuilder sql = new StringBuilder("SELECT function.*, users.username AS owner_name FROM function JOIN users ON function.owner_id = users.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null) {
            sql.append(" AND function.name = ?");
            params.add(name);
        }

        if (type != null) {
            sql.append(" AND function.type = ?");
            params.add(type.toString());
        }

        if (ownerId != null) {
            sql.append(" AND function.owner_id = ?");
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

    public void update(Function function) throws SQLException {
        String sql = "UPDATE function SET name = ?, type = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, function.getName());
            ps.setString(2, function.getType().toString());
            ps.setLong(3, function.getId());

            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM function WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}