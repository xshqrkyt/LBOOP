package com.lab5.dao;

import com.lab5.entity.CompositeFunctionLink;

import org.junit.jupiter.api.*;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.util.*;

public class CompositeFunctionLinkDAOTest {
    private Connection connection;
    private CompositeFunctionLinkDAO compositeFunctionLinkDAO;
    private CompositeFunctionDAO compositeFunctionDAO;

    private Long userId;

    @BeforeEach
    public void setup() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");

        try (Statement st = connection.createStatement()) {
            // Создаем таблицы
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    email VARCHAR(100),
                    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );

                CREATE TABLE IF NOT EXISTS function (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    owner_id BIGINT NOT NULL,
                    
                    CONSTRAINT fk_owner FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE
                );

                CREATE TABLE IF NOT EXISTS composite_function (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    owner_id BIGINT NOT NULL,
                    
                    CONSTRAINT cfk_owner FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE
                );

                CREATE TABLE IF NOT EXISTS composite_function_link (
                    id BIGSERIAL PRIMARY KEY,
                    composite_id BIGINT NOT NULL,
                    function_id BIGINT NOT NULL,
                    order_index INT NOT NULL,
                    
                    CONSTRAINT cfk_composite FOREIGN KEY(composite_id) REFERENCES composite_function(id) ON DELETE CASCADE,
                    CONSTRAINT cfk_function FOREIGN KEY(function_id) REFERENCES function(id) ON DELETE CASCADE
                );
            """);

            // Вставляем пользователя
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO users(username, password_hash, email, role) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "owner");
                ps.setString(2, "pass");
                ps.setString(3, "owner@example.com");
                ps.setString(4, "USER");
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    userId = rs.getLong(1);
                }
            }

            // Вставляем запись в функцию для foreign key
            st.execute("INSERT INTO function (name, type, owner_id) VALUES ('testFunction', 'SQR', " + userId + ")");
            // Вставляем запись в composite_function для foreign key
            st.execute("INSERT INTO composite_function (name, owner_id) VALUES ('testCompositeFunction', " + userId + ")");
        }

        compositeFunctionLinkDAO = new CompositeFunctionLinkDAO(connection);
        compositeFunctionDAO = new CompositeFunctionDAO(connection);
    }

    @Test
    public void methodsTest() throws Exception {
        // Получаем ids composite_function и функции из БД
        Long compositeId;
        Long functionId;

        try (Statement st = connection.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT id FROM composite_function LIMIT 1")) {
                rs.next();
                compositeId = rs.getLong(1);
            }
            try (ResultSet rs = st.executeQuery("SELECT id FROM function LIMIT 1")) {
                rs.next();
                functionId = rs.getLong(1);
            }
        }

        CompositeFunctionLink link1 = new CompositeFunctionLink(null, compositeId, functionId, 0);
        CompositeFunctionLink link2 = new CompositeFunctionLink(null, compositeId, functionId, 1);

        Long linkId1 = compositeFunctionLinkDAO.create(link1);
        assertNotNull(linkId1);

        Long linkId2 = compositeFunctionLinkDAO.create(link2);
        assertNotNull(linkId2);

        CompositeFunctionLink found1 = compositeFunctionLinkDAO.findId(linkId1);
        CompositeFunctionLink found2 = compositeFunctionLinkDAO.findId(linkId2);

        assertEquals(0, found1.getOrderIndex());
        assertEquals(1, found2.getOrderIndex());

        // Обновление order_index для первой ссылки
        found1.setOrderIndex(5);
        compositeFunctionLinkDAO.update(found1);

        CompositeFunctionLink updated1 = compositeFunctionLinkDAO.findId(linkId1);
        assertEquals(5, updated1.getOrderIndex());

        // Удаление второй ссылки
        compositeFunctionLinkDAO.delete(linkId2);
        assertNull(compositeFunctionLinkDAO.findId(linkId2));
        assertNotNull(compositeFunctionLinkDAO.findId(linkId1));
    }

    @Test
    public void testDepthFirstSearch() throws Exception {
        // Подготовка: несколько функций и ссылок, образующих структуру связей
        Long compositeId = getAnyCompositeFunctionId();
        Long funcId1 = createFunction("Func1");
        Long funcId2 = createFunction("Func2");
        Long funcId3 = createFunction("Func3");

        compositeFunctionLinkDAO.create(new CompositeFunctionLink(null, compositeId, funcId1, 0));
        compositeFunctionLinkDAO.create(new CompositeFunctionLink(null, compositeId, funcId2, 1));
        compositeFunctionLinkDAO.create(new CompositeFunctionLink(null, compositeId, funcId3, 2));// вложенная связь

        List<Long> dfsResult = compositeFunctionLinkDAO.dfsFunctions(compositeId);
        assertTrue(dfsResult.contains(funcId1));
        assertTrue(dfsResult.contains(funcId2));
        assertTrue(dfsResult.contains(funcId3));
    }

    @Test
    public void testBreadthFirstSearch() throws Exception {
        Long compositeId = getAnyCompositeFunctionId();
        Long funcId1 = createFunction("FuncBFS1");
        Long funcId2 = createFunction("FuncBFS2");

        compositeFunctionLinkDAO.create(new CompositeFunctionLink(null, compositeId, funcId1, 0));
        compositeFunctionLinkDAO.create(new CompositeFunctionLink(null, compositeId, funcId2, 1));

        List<Long> bfsResult = compositeFunctionLinkDAO.bfsFunctions(compositeId);
        assertTrue(bfsResult.contains(funcId1));
        assertTrue(bfsResult.contains(funcId2));
    }

    @Test
    public void testFindByIdsAndSorted() throws Exception {
        Long compositeId = getAnyCompositeFunctionId();
        Long funcId = createFunction("FuncSort");

        Long id1 = compositeFunctionLinkDAO.create(new CompositeFunctionLink(null, compositeId, funcId, 1));
        Long id2 = compositeFunctionLinkDAO.create(new CompositeFunctionLink(null, compositeId, funcId, 2));

        List<Long> ids = List.of(id1, id2);

        List<CompositeFunctionLink> foundLinks = compositeFunctionLinkDAO.findIds(ids);
        assertEquals(2, foundLinks.size());

        List<CompositeFunctionLink> sortedLinks = compositeFunctionLinkDAO.findAllSorted("order_index", SortOrder.ASCENDING);
        assertTrue(sortedLinks.size() >= 2);

        for (int i = 1; i < sortedLinks.size(); ++i)
            assertTrue(sortedLinks.get(i - 1).getOrderIndex() <= sortedLinks.get(i).getOrderIndex());
    }

    // Дополнительные методы поддержки в тестах
    private Long createFunction(String name) throws SQLException {
        String sql = "INSERT INTO function (name, type, owner_id) VALUES (?, 'SQR', ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setLong(2, userId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private Long getAnyCompositeFunctionId() throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM composite_function LIMIT 1")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    @AfterEach
    public void close() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM composite_function_link");
            st.execute("DELETE FROM composite_function");
            st.execute("DELETE FROM function");
            st.execute("DELETE FROM users");
        }

        if (connection != null && !connection.isClosed())
            connection.close();
    }
}