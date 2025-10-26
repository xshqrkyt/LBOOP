package com.lab5.dao;

import com.lab5.entity.User;
import com.lab5.enums.UserRole;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public class UserDAO implements DAO<User> {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Long create(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, email, role) VALUES (?, ?, ?, ?);";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());

            ps.executeUpdate();

            try (ResultSet Keys = ps.getGeneratedKeys()) {
                if (Keys.next())
                    return Keys.getLong("id"); // Установить id после вставки
            }
        }

        throw new SQLException("Ошибка при создании пользователя.");
    }

    // Одиночный поиск по id
    public User findId(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new User(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"), rs.getString("email"), UserRole.valueOf(rs.getString("role")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);

                return null;
            }
        }
    }

    // Одиночный поиск по имени пользователя
    public User findUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new User(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"), rs.getString("email"), UserRole.valueOf(rs.getString("role")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);

                return null;
            }
        }
    }

    // Множественный поиск (поиск по критериям)
    public List<User> findCriteria(String username, String email, UserRole role) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (username != null) {
            sql.append(" AND username = ?");
            params.add(username);
        }

        if (email != null) {
            sql.append(" AND email = ?");
            params.add(email);
        }

        if (role != null) {
            sql.append(" AND role = ?");
            params.add(role.name());
        }

        int n = params.size();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < n; ++i)
                ps.setObject(i + 1, params.get(i));

            List<User> users = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    users.add(new User(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"), rs.getString("email"), UserRole.valueOf(rs.getString("role")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null));

            }

            return users;
        }
    }

    // Поиск имени пользователя с сортировкой по выбранному столбцу
    public User findUsernameSorted(String username, String sortByColumn, SortOrder order) throws SQLException {
        Set<String> allowedSortColumns = Set.of("id", "username", "email", "created_at");
        if (!allowedSortColumns.contains(sortByColumn))
            throw new IllegalArgumentException("Недопустимое поле сортировки.");

        String orderDirection = order == SortOrder.ASCENDING ? "ASC" : "DESC";

        String sql = "SELECT * FROM users WHERE username = ? ORDER BY " + sortByColumn + " " + orderDirection;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new User(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"), rs.getString("email"), UserRole.valueOf(rs.getString("role")), rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);

                return null;
            }
        }
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET email = ?, password_hash = ?, role = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setLong(4, user.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}