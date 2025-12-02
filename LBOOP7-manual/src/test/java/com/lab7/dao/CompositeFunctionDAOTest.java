package com.lab7.dao;

import com.lab7.entity.CompositeFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CompositeFunctionDAOTest {
    private Connection connection;
    private CompositeFunctionDAO compositeFunctionDAO;

    private static final Logger logger = LogManager.getLogger(CompositeFunctionDAOTest.class);

    private Long userId1;
    private Long userId2;

    @BeforeEach
    public void setup() throws Exception {
        logger.info("Подключаемся к H2 с эмуляцией PostgreSQL и создаём таблицы.");

        String[] tables = {"src/main/resources/scripts/tables/users_table.sql", "src/main/resources/scripts/tables/function_table.sql", "src/main/resources/scripts/tables/composite_function_table.sql"};
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");

        try (Statement st = connection.createStatement()) {
            for (String script : tables) {
                String sql = new String(Files.readAllBytes(Paths.get(script)));

                st.execute(sql);
            }
        }

        // Вставляем пользователей и получаем их id
        userId1 = insertUser("user1", "pass1", "user1@example.com", "USER");
        userId2 = insertUser("user2", "pass2", "user2@example.com", "ADMIN");

        compositeFunctionDAO = new CompositeFunctionDAO(connection);

        logger.info("Таблицы созданы, пользователи вставлены: " + userId1 + ", " + userId2);
    }

    private Long insertUser(String username, String password, String email, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, email, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, role);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getLong(1);
                else
                    throw new SQLException("Не удалось получить id пользователя");
            }
        }
    }

    @Test
    public void methodsTest() throws Exception {
        logger.info("Тестируем создание, поиск, обновление и удаление CompositeFunction.");

        // Создаём три функции с двумя разными владельцами и двумя именами
        LocalDateTime now = LocalDateTime.now();

        CompositeFunction compF1 = new CompositeFunction(null, "CompositeName1", now, userId1);
        CompositeFunction compF2 = new CompositeFunction(null, "CompositeName2", now, userId2);
        CompositeFunction compF3 = new CompositeFunction(null, "CompositeName1", now, userId2);

        Long id1 = compositeFunctionDAO.create(compF1);
        assertNotNull(id1);

        Long id2 = compositeFunctionDAO.create(compF2);
        assertNotNull(id2);

        Long id3 = compositeFunctionDAO.create(compF3);
        assertNotNull(id3);

        // Проверка поиска по id
        CompositeFunction f1 = compositeFunctionDAO.findId(id1);
        CompositeFunction f2 = compositeFunctionDAO.findId(id2);
        CompositeFunction f3 = compositeFunctionDAO.findId(id3);

        assertEquals("CompositeName1", f1.getName());
        assertEquals(userId1, f1.getOwnerId());

        assertEquals("CompositeName2", f2.getName());
        assertEquals(userId2, f2.getOwnerId());

        assertEquals("CompositeName1", f3.getName());
        assertEquals(userId2, f3.getOwnerId());

        // Проверка метода findName, который возвращает список функций с заданным именем
        List<CompositeFunction> foundList1 = compositeFunctionDAO.findName("CompositeName1");
        assertNotNull(foundList1);
        assertEquals(2, foundList1.size());

        // Проверка метода findName, который возвращает список функций с заданным именем
        List<CompositeFunction> foundList2 = compositeFunctionDAO.findName("CompositeName2");
        assertNotNull(foundList2);
        assertEquals(1, foundList2.size());

        List<Long> foundIds = foundList1.stream().map(CompositeFunction::getId).collect(Collectors.toList());
        assertTrue(foundIds.contains(id1));
        assertTrue(foundIds.contains(id3));

        // Обновление имени первой функции
        f1.setName("UpdatedCompositeName");
        compositeFunctionDAO.update(f1);

        CompositeFunction updated = compositeFunctionDAO.findId(id1);
        assertEquals("UpdatedCompositeName", updated.getName());

        // Удаление второй функции
        compositeFunctionDAO.delete(id2);
        assertNull(compositeFunctionDAO.findId(id2));

        assertNotNull(compositeFunctionDAO.findId(id1));
        assertNotNull(compositeFunctionDAO.findId(id3));
    }

    // Множественный поиск по именам
    @Test
    public void findNamesTest() throws Exception {
        // Добавим функции
        LocalDateTime now = LocalDateTime.now();
        compositeFunctionDAO.create(new CompositeFunction(null, "Name1", now, userId1));
        compositeFunctionDAO.create(new CompositeFunction(null, "Name2", now, userId2));
        compositeFunctionDAO.create(new CompositeFunction(null, "Name1", now, userId2));

        List<String> names = List.of("Name1", "Name2");
        List<CompositeFunction> results = compositeFunctionDAO.findNames(names);

        assertNotNull(results);
        assertTrue(results.size() >= 3);
        for (CompositeFunction cf : results)
            assertTrue(names.contains(cf.getName()));
    }

    // Сортировка по полям с SortOrder
    @Test
    public void findByNamesSortedTest() throws Exception {
        // Создаём несколько composite functions с разными именами и владельцами
        CompositeFunction cf1 = new CompositeFunction(null, "Name1", LocalDateTime.now(), userId1);
        compositeFunctionDAO.create(cf1);

        CompositeFunction cf2 = new CompositeFunction(null, "Name2", LocalDateTime.now(), userId2);
        compositeFunctionDAO.create(cf2);

        CompositeFunction cf3 = new CompositeFunction(null, "Name1", LocalDateTime.now(), userId2);
        compositeFunctionDAO.create(cf3);

        CompositeFunction cf4 = new CompositeFunction(null, "Name3", LocalDateTime.now(), userId1);
        compositeFunctionDAO.create(cf4);

        // Поиск по имени с сортировкой по имени по возрастанию
        List<String> searchNames = List.of("Name1", "Name2");
        List<CompositeFunction> results = compositeFunctionDAO.findAllSorted(searchNames, "name", SortOrder.ASCENDING);

        assertNotNull(results);
        assertTrue(results.size() >= 3);

        // Проверяем что все имена из списка
        for (CompositeFunction cf : results)
            assertTrue(searchNames.contains(cf.getName()));

        // Проверяем сортировку по имени (ASC)
        for (int i = 1; i < results.size(); ++i)
            assertTrue(results.get(i - 1).getName().compareTo(results.get(i).getName()) <= 0);

        // Поиск с сортировкой по созданию по убыванию
        results = compositeFunctionDAO.findAllSorted(searchNames, "created_at", SortOrder.DESCENDING);

        assertNotNull(results);

        for (int i = 1; i < results.size(); ++i)
            assertTrue(results.get(i - 1).getCreatedAt().compareTo(results.get(i).getCreatedAt()) >= 0);

        // Проверка, что ошибка выбрасывается при неправильном поле сортировки
        assertThrows(IllegalArgumentException.class, () -> compositeFunctionDAO.findAllSorted(searchNames, "invalid_field", SortOrder.ASCENDING));
    }

    // Поиск по владельцу (иерархия)
    @Test
    public void findOwnerIdTest() throws Exception {
        List<CompositeFunction> user1Funcs = compositeFunctionDAO.findOwnerId(userId1);
        assertNotNull(user1Funcs);
        for (CompositeFunction cf : user1Funcs)
            assertEquals(userId1, cf.getOwnerId());

        List<CompositeFunction> user2Functions = compositeFunctionDAO.findOwnerId(userId2);
        assertNotNull(user2Functions);
        for (CompositeFunction cf : user2Functions)
            assertEquals(userId2, cf.getOwnerId());
    }

    @AfterEach
    public void close() throws Exception {
        logger.info("Очищаем таблицы после каждого теста.");

        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM composite_function");
            st.execute("DELETE FROM functions");
            st.execute("DELETE FROM users");
        }

        logger.info("Закрываем соединение.");
        if (connection != null && !connection.isClosed())
            connection.close();
    }
}