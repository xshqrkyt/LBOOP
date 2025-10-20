package ru.ssau.tk.samsa.lb6.jdbc.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.samsa.lb6.jdbc.dto.PointDto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PointDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(PointDao.class);
    private final Connection connection;

    public PointDao(Connection connection) {
        this.connection = connection;
    }

    public Long insertPoint(Long functionId, double x, double y, int index) throws SQLException {
        String sql = "INSERT INTO points(function_id, x_value, y_value, point_index) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, functionId);
            ps.setDouble(2, x);
            ps.setDouble(3, y);
            ps.setInt(4, index);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    LOGGER.debug("Inserted point id={} f={} idx={}", id, functionId, index);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to insert point");
    }

    public void insertPointsBatch(Long functionId, List<PointDto> points) throws SQLException {
        String sql = "INSERT INTO points(function_id, x_value, y_value, point_index) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            for (PointDto p : points) {
                ps.setLong(1, functionId);
                ps.setDouble(2, p.getX());
                ps.setDouble(3, p.getY());
                ps.setInt(4, p.getIndex());
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
            LOGGER.info("Batch inserted {} points for function {}", points.size(), functionId);
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public List<PointDto> findByFunctionIdOrdered(long functionId) throws SQLException {
        String sql = "SELECT id, function_id, x_value, y_value, point_index FROM points WHERE function_id = ? ORDER BY point_index";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, functionId);
            try (ResultSet rs = ps.executeQuery()) {
                List<PointDto> pts = new ArrayList<>();
                while (rs.next()) {
                    PointDto p = new PointDto();
                    p.setId(rs.getLong("id"));
                    p.setFunctionId(functionId);
                    p.setX(rs.getDouble("x_value"));
                    p.setY(rs.getDouble("y_value"));
                    p.setIndex(rs.getInt("point_index"));
                    pts.add(p);
                }
                LOGGER.info("Found {} points for function {}", pts.size(), functionId);
                return pts;
            }
        }
    }

    public void updateY(Long id, double y) throws SQLException {
        String sql = "UPDATE points SET y_value = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, y);
            ps.setLong(2, id);
            int upd = ps.executeUpdate();
            LOGGER.debug("Updated point {} y, rows={}", id, upd);
        }
    }

    public void deletePoint(Long id) throws SQLException {
        String sql = "DELETE FROM points WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            int del = ps.executeUpdate();
            LOGGER.debug("Deleted point {} rows={}", id, del);
        }
    }
}
