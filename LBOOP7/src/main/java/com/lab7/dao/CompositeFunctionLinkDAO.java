package com.lab7.dao;

import com.lab7.entity.CompositeFunctionLink;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeFunctionLinkDAO implements DAO<CompositeFunctionLink> {
    private final Connection connection;

    public CompositeFunctionLinkDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Long create(CompositeFunctionLink compFunctionLink) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/insert/insert_composite_function_link.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/insert/insert_composite_function_link.sql");

        String insert = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
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
    public CompositeFunctionLink findId(Long id) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_composite_function_link_id.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_composite_function_link_id.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new CompositeFunctionLink(rs.getLong("id"), rs.getLong("composite_id"), rs.getLong("function_id"), rs.getInt("order_index"));

                return null;
            }
        }
    }

    // Одиночный поиск по id сложной функции
    public List<CompositeFunctionLink> findByCompositeFunctionId(Long compositeFunctionId) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_composite_function_link_comp_f_id.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_composite_function_link_comp_f_id.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setLong(1, compositeFunctionId);
            List<CompositeFunctionLink> result = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new CompositeFunctionLink(rs.getLong("id"), rs.getLong("composite_id"), rs.getLong("function_id"), rs.getInt("order_index")));
            }

            return result;
        }
    }

    // Одиночный поиск по id функции
    public List<CompositeFunctionLink> findByFunctionId(Long functionId) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/find/find_composite_function_link_function_id.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/find/find_composite_function_link_function_id.sql");

        String find = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(find)) {
            ps.setLong(1, functionId);
            List<CompositeFunctionLink> result = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new CompositeFunctionLink(rs.getLong("id"), rs.getLong("composite_id"), rs.getLong("function_id"), rs.getInt("order_index")));
            }

            return result;
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

    @Override
    public void update(CompositeFunctionLink compFunctionLink) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/update/update_composite_function_link.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/update/update_composite_function_link.sql");

        String update = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setLong(1, compFunctionLink.getOrderIndex());
            ps.setLong(2, compFunctionLink.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts/delete/delete_composite_function_link.sql");
        if (is == null)
            throw new FileNotFoundException("Resource not found: scripts/delete/delete_composite_function_link.sql");

        String delete = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (PreparedStatement ps = connection.prepareStatement(delete)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}