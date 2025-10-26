package com.lab5.dao;

import com.lab5.entity.CompositeFunctionLink;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public class CompositeFunctionLinkDAO implements DAO<CompositeFunctionLink> {
    private final Connection connection;

    public CompositeFunctionLinkDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Long create(CompositeFunctionLink compFunctionLink) throws SQLException {
        String sql = "INSERT INTO composite_function_link (composite_id, function_id, order_index) VALUES (?, ?, ?);";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, compFunctionLink.getCompositeId());
            ps.setLong(2, compFunctionLink.getFunctionId());
            ps.setLong(3, compFunctionLink.getOrderIndex());

            ps.executeUpdate();

            try (ResultSet Keys = ps.getGeneratedKeys()) {
                if (Keys.next())
                    return Keys.getLong("id"); // Установить id после вставки
            }
        }

        throw new SQLException("Ошибка при создании ссылки на функции.");
    }

    // Одиночный поиск по id
    public CompositeFunctionLink findId(Long id) throws SQLException {
        String sql = "SELECT composite_function_link.*, function.name AS function_name, composite_function.name AS composite_name FROM composite_function_link JOIN function ON composite_function_link.function_id = function.id JOIN composite_function ON composite_function_link.composite_id = composite_function.id WHERE composite_function_link.id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new CompositeFunctionLink(rs.getLong("id"), rs.getLong("composite_id"), rs.getLong("function_id"), rs.getInt("order_index"));

                return null;
            }
        }
    }

    // Поиск множества по списку id
    public List<CompositeFunctionLink> findIds(List<Long> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return List.of();

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT * FROM composite_function_link WHERE id IN (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); ++i)
                ps.setLong(i + 1, ids.get(i));

            List<CompositeFunctionLink> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new CompositeFunctionLink(rs.getLong("id"), rs.getLong("composite_id"), rs.getLong("function_id"), rs.getInt("order_index")));
            }

            return result;
        }
    }

    // Поиск всех с сортировкой по полю (например, order_index)
    public List<CompositeFunctionLink> findAllSorted(String sortBy, SortOrder sortOrder) throws SQLException {
        List<String> allowedFields = List.of("id", "composite_id", "function_id", "order_index");
        if (sortBy == null || !allowedFields.contains(sortBy.toLowerCase()))
            throw new IllegalArgumentException("Недопустимое поле сортировки: " + sortBy);

        String order = (sortOrder == SortOrder.DESCENDING) ? "DESC" : "ASC";

        String sql = "SELECT * FROM composite_function_link ORDER BY " + sortBy + " " + order;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<CompositeFunctionLink> list = new ArrayList<>();
            while (rs.next())
                list.add(new CompositeFunctionLink(rs.getLong("id"), rs.getLong("composite_id"), rs.getLong("function_id"), rs.getInt("order_index")));

            return list;
        }
    }

    // Поиск функций, связанных с composite_id, рекурсивно (DFS)
    public List<Long> dfsFunctions(Long compositeId) throws SQLException {
        Set<Long> visited = new HashSet<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(compositeId);

        while (!stack.isEmpty()) {
            Long current = stack.pop();

            try (PreparedStatement ps = connection.prepareStatement("SELECT function_id FROM composite_function_link WHERE composite_id = ?")) {
                ps.setLong(1, current);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Long funcId = rs.getLong("function_id");
                        if (visited.add(funcId))
                            stack.push(funcId);
                    }
                }
            }
        }

        return new ArrayList<>(visited);
    }

    // Поиск функций связных через composite_id, обход уровней (BFS)
    public List<Long> bfsFunctions(Long compositeId) throws SQLException {
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(compositeId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();

            try (PreparedStatement ps = connection.prepareStatement("SELECT function_id FROM composite_function_link WHERE composite_id = ?")) {
                ps.setLong(1, current);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Long funcId = rs.getLong("function_id");
                        if (visited.add(funcId))
                            queue.add(funcId);
                    }
                }
            }
        }

        return new ArrayList<>(visited);
    }

    @Override
    public void update(CompositeFunctionLink compFunctionLink) throws SQLException {
        String sql = "UPDATE composite_function_link SET order_index = ? WHERE id = ?;";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, compFunctionLink.getOrderIndex());
            ps.setLong(2, compFunctionLink.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM composite_function_link WHERE id = ?;";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}