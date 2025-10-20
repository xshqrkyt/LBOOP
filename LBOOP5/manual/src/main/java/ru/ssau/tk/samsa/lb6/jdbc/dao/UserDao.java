package ru.ssau.tk.samsa.lb6.jdbc.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.samsa.lb6.jdbc.dto.UserDto;

import java.sql.*;
import java.util.Optional;

public class UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);
    private final Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public Optional<UserDto> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, email, role, created_at FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserDto u = new UserDto();
                    u.setId(rs.getLong("id"));
                    u.setUsername(rs.getString("username"));
                    u.setEmail(rs.getString("email"));
                    u.setRole(rs.getString("role"));
                    LOGGER.info("Found user {}", username);
                    return Optional.of(u);
                }
            }
        }
        LOGGER.info("User {} not found", username);
        return Optional.empty();
    }

    public Long createUser(String username, String passwordHash, String email, String role) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, email, role) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, email);
            ps.setString(4, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    LOGGER.info("Created user {} with id {}", username, id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to insert user");
    }

    public void updateEmail(Long id, String email) throws SQLException {
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setLong(2, id);
            int updated = ps.executeUpdate();
            LOGGER.info("Updated user {} email, rows={} ", id, updated);
        }
    }

    public void deleteUser(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            int deleted = ps.executeUpdate();
            LOGGER.info("Deleted user {}, rows={} ", id, deleted);
        }
    }
}
