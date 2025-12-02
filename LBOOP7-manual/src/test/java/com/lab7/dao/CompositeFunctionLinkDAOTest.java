package com.lab7.dao;

import com.lab7.entity.CompositeFunctionLink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CompositeFunctionLinkDAOTest {
    private Connection connection;
    private CompositeFunctionLinkDAO compositeFunctionLinkDAO;
    private CompositeFunctionDAO compositeFunctionDAO;

    private Long userId;

    @BeforeEach
    public void setup() throws Exception {
        String[] tables = {"src/main/resources/scripts/tables/users_table.sql", "src/main/resources/scripts/tables/function_table.sql", "src/main/resources/scripts/tables/composite_function_table.sql", "src/main/resources/scripts/tables/composite_function_link_table.sql"};
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");

        try (Statement st = connection.createStatement()) {
            for (String script : tables) {
                String sql = new String(Files.readAllBytes(Paths.get(script)));

                st.execute(sql);
            }

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
            st.execute("INSERT INTO functions (name, type, owner_id) VALUES ('testFunction', 'SQR', " + userId + ")");
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
            try (ResultSet rs = st.executeQuery("SELECT id FROM functions LIMIT 1")) {
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
    public void testFindCompositeFunctionId() throws Exception {
        Long compositeId = getAnyCompositeFunctionId();
        Long funcId1 = createFunction("Function1");
        Long funcId2 = createFunction("Function2");

        CompositeFunctionLink link1 = new CompositeFunctionLink(null, compositeId, funcId1, 0);
        CompositeFunctionLink link2 = new CompositeFunctionLink(null, compositeId, funcId2, 1);

        Long id1 = compositeFunctionLinkDAO.create(link1);
        Long id2 = compositeFunctionLinkDAO.create(link2);

        List<CompositeFunctionLink> links = compositeFunctionLinkDAO.findByCompositeFunctionId(compositeId);
        assertNotNull(links);
        assertTrue(links.size() >= 2);
        Set<Long> linkIds = new HashSet<>();
        for (CompositeFunctionLink link : links) {
            assertEquals(compositeId, link.getCompositeId());
            linkIds.add(link.getId());
        }

        assertTrue(linkIds.contains(id1));
        assertTrue(linkIds.contains(id2));
    }

    @Test
    public void testFindFunctionId() throws Exception {
        Long compositeId1 = getAnyCompositeFunctionId();
        Long compositeId2 = getAnyCompositeFunctionId();
        Long funcId = createFunction("FuncSingle");

        CompositeFunctionLink link1 = new CompositeFunctionLink(null, compositeId1, funcId, 0);
        CompositeFunctionLink link2 = new CompositeFunctionLink(null, compositeId2, funcId, 1);

        Long id1 = compositeFunctionLinkDAO.create(link1);
        Long id2 = compositeFunctionLinkDAO.create(link2);

        List<CompositeFunctionLink> links = compositeFunctionLinkDAO.findByFunctionId(funcId);
        assertNotNull(links);
        assertTrue(links.size() >= 2);
        for (CompositeFunctionLink link : links)
            assertEquals(funcId, link.getFunctionId());

        Set<Long> linkIds = new HashSet<>();
        for (CompositeFunctionLink link : links)
            linkIds.add(link.getId());

        assertTrue(linkIds.contains(id1));
        assertTrue(linkIds.contains(id2));
    }

    @Test
    public void testFindIdsAndSorted() throws Exception {
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
        String sql = "INSERT INTO functions (name, type, owner_id) VALUES (?, 'SQR', ?)";
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
            st.execute("DELETE FROM functions");
            st.execute("DELETE FROM users");
        }

        if (connection != null && !connection.isClosed())
            connection.close();
    }
}