package ru.ssau.tk.samsa.lb6.jdbc.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.samsa.lb6.jdbc.dto.FunctionDto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FunctionDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionDao.class);
    private final Connection connection;

    public FunctionDao(Connection connection) {
        this.connection = connection;
    }

    public Long createFunction(Long ownerId, String name, String type) throws SQLException {
        String sql = "INSERT INTO functions(owner_id, name, type) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            ps.setString(2, name);
            ps.setString(3, type);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    LOGGER.info("Created function {} id={} for owner {}", name, id, ownerId);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to insert function");
    }

    public List<FunctionDto> findByOwnerId(long ownerId) throws SQLException {
        String sql = "SELECT id, owner_id, name, type, created_at FROM functions WHERE owner_id = ? ORDER BY created_at";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<FunctionDto> list = new ArrayList<>();
                while (rs.next()) {
                    FunctionDto f = new FunctionDto();
                    f.setId(rs.getLong("id"));
                    f.setOwnerId(ownerId);
                    f.setName(rs.getString("name"));
                    f.setType(rs.getString("type"));
                    list.add(f);
                }
                LOGGER.info("Found {} functions for owner {}", list.size(), ownerId);
                return list;
            }
        }
    }

    public void updateName(Long id, String newName) throws SQLException {
        String sql = "UPDATE functions SET name = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setLong(2, id);
            int upd = ps.executeUpdate();
            LOGGER.info("Updated function {} name, rows={}", id, upd);
        }
    }

    public void deleteFunction(Long id) throws SQLException {
        String sql = "DELETE FROM functions WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            int del = ps.executeUpdate();
            LOGGER.info("Deleted function {}, rows={}", id, del);
        }
    }
}
