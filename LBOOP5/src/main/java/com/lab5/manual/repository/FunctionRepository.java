package com.lab5.manual.repository;

import com.lab5.manual.entity.Function;
import com.lab5.manual.entity.Point;
import com.lab5.manual.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FunctionRepository {
    private static final Logger logger = LoggerFactory.getLogger(FunctionRepository.class);
    private final DatabaseConfig databaseConfig;
    private final PointRepository pointRepository;

    public FunctionRepository(DatabaseConfig databaseConfig, PointRepository pointRepository) {
        this.databaseConfig = databaseConfig;
        this.pointRepository = pointRepository;
    }

    public Long create(Function function) {
        String sql = "INSERT INTO function (name, type, owner_id) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, function.getName());
            stmt.setString(2, function.getType());
            stmt.setLong(3, function.getOwnerId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                logger.info("Created function with id: {}", id);
                return id;
            }
        } catch (SQLException e) {
            logger.error("Error creating function", e);
            throw new RuntimeException("Database error", e);
        }
        return null;
    }

    public Optional<Function> findById(Long id) {
        String sql = "SELECT * FROM function WHERE id = ?";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToFunction(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding function by id: {}", id, e);
        }
        return Optional.empty();
    }

    public List<Function> findByOwnerId(Long ownerId) {
        List<Function> functions = new ArrayList<>();
        String sql = "SELECT * FROM function WHERE owner_id = ? ORDER BY created_at DESC";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, ownerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToFunction(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding functions by owner: {}", ownerId, e);
        }
        return functions;
    }

    public List<Function> findByNameContaining(String name) {
        List<Function> functions = new ArrayList<>();
        String sql = "SELECT * FROM function WHERE name ILIKE ? ORDER BY name";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToFunction(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding functions by name: {}", name, e);
        }
        return functions;
    }

    public List<Function> findAllWithPointsByOwner(Long ownerId) {
        List<Function> functions = findByOwnerId(ownerId);

        for (Function function : functions) {
            List<Point> points = pointRepository.findByFunctionId(function.getId());
            // Здесь можно добавить точки к функции если нужно
        }

        return functions;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM function WHERE id = ?";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            logger.info("Deleted {} functions with id: {}", affectedRows, id);
        } catch (SQLException e) {
            logger.error("Error deleting function with id: {}", id, e);
        }
    }

    private Function mapResultSetToFunction(ResultSet rs) throws SQLException {
        Function function = new Function();
        function.setId(rs.getLong("id"));
        function.setName(rs.getString("name"));
        function.setType(rs.getString("type"));
        function.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        function.setOwnerId(rs.getLong("owner_id"));
        return function;
    }
}
