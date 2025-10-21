package com.lab5.manual.repository;

import com.lab5.manual.entity.Point;
import com.lab5.manual.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PointRepository {
    private static final Logger logger = LoggerFactory.getLogger(PointRepository.class);
    private final DatabaseConfig databaseConfig;

    public PointRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public Long create(com.lab5.manual.entity.Point point) {
        String sql = "INSERT INTO point (x_value, y_value, function_id) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, point.getXValue());
            stmt.setDouble(2, point.getYValue());
            stmt.setLong(3, point.getFunctionId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
        } catch (SQLException e) {
            logger.error("Error creating point", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Point> findByFunctionId(Long functionId) {
        List<Point> points = new ArrayList<>();
        String sql = "SELECT * FROM point WHERE function_id = ? ORDER BY id";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Point p = new Point();
                p.setId(rs.getLong("id"));
                p.setXValue(rs.getDouble("x_value"));
                p.setYValue(rs.getDouble("y_value"));
                p.setFunctionId(rs.getLong("function_id"));
                points.add(p);
            }
        } catch (SQLException e) {
            logger.error("Error finding points for function: {}", functionId, e);
        }
        return points;
    }
}
