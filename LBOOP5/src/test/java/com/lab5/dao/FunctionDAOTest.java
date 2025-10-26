package com.lab5.dao;

import com.lab5.enums.FunctionType;
import com.lab5.enums.UserRole;
import com.lab5.entity.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class FunctionDAOTest {
    private Connection connection;
    private FunctionDAO functionDAO;
    private UserDAO userDAO;

    private Long user1;
    private Long user2;

    private static final Logger logger = LogManager.getLogger(FunctionDAOTest.class);

    @BeforeEach
    void setup() throws SQLException {
        logger.info("Подключаемся к H2 с эмуляцией PostgreSQL.");

        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");

        try (var st = connection.createStatement()) {
            logger.info("Создаём таблицы.");

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
                    type VARCHAR(50) NOT NULL CHECK (type IN ('SQR', 'IDENTITY', 'COMPOSITE', 'CONSTANT', 'NEWTON_METHOD', 'DEBOOR', 'SIN')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    owner_id BIGINT NOT NULL,
                    
                    CONSTRAINT fk_owner FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);
        }

        logger.info("Таблицы созданы.");

        userDAO = new UserDAO(connection);
        functionDAO = new FunctionDAO(connection);

        logger.info("Создаём пользователей для БД.");

        // Создаем двух владельцев
        user1 = userDAO.create(new User(null, "owner1", "password1", "user1@example.com", UserRole.USER, LocalDateTime.now()));
        user2 = userDAO.create(new User(null, "owner2", "password2", "user2@example.com", UserRole.ADMIN, LocalDateTime.now()));
        assertNotNull(user1);
        assertNotNull(user2);
    }

    @Test
    public void methodsTest() throws SQLException {
        logger.info("Тестируются создание в БД функций, поиск их по id, имени, изменение данных у них и удаление их из БД.");

        // Создаём функции
        Function f1 = new Function(null, "Function1", FunctionType.SQR, LocalDateTime.now(), user1);
        Function f2 = new Function(null, "Function1", FunctionType.IDENTITY, LocalDateTime.now(), user2);
        Function f3 = new Function(null, "Function2", FunctionType.SIN, LocalDateTime.now(), user1);
        Function f4 = new Function(null, "Function1", FunctionType.NEWTON_METHOD, LocalDateTime.now(), user1);

        Long id1 = functionDAO.create(f1);
        assertNotNull(id1);

        Long id2 = functionDAO.create(f2);
        assertNotNull(id2);

        Long id3 = functionDAO.create(f3);
        assertNotNull(id3);

        Long id4 = functionDAO.create(f4);
        assertNotNull(id4);

        // Поиск функции по id
        Function found1 = functionDAO.findId(id1);
        assertNotNull(found1);
        assertEquals("Function1", found1.getName());
        assertEquals(FunctionType.SQR, found1.getType());

        // Поиск функции по имени (должно вернуть 3 функции с именем "Function1")
        List<Function> listByName = functionDAO.findName("Function1");
        assertEquals(3, listByName.size());

        List<Long> idsByName = listByName.stream().map(Function::getId).collect(Collectors.toList());

        assertTrue(idsByName.contains(id1));
        assertTrue(idsByName.contains(id2));
        assertTrue(idsByName.contains(id4));

        // Поиск функций по имени владельца user1 (функции id1, id3 и id4)
        List<Function> listByOwner1 = functionDAO.findOwnerId(user1);
        assertEquals(3, listByOwner1.size());

        List<Long> idsByOwner1 = listByOwner1.stream().map(Function::getId).collect(Collectors.toList());
        assertTrue(idsByOwner1.contains(id1));
        assertTrue(idsByOwner1.contains(id3));
        assertTrue(idsByOwner1.contains(id4));

        // Поиск функций по имени владельца user2 (функция id2)
        List<Function> listByOwner2 = functionDAO.findOwnerId(user2);
        assertEquals(1, listByOwner2.size());
        assertEquals(id2, listByOwner2.get(0).getId());

        // Обновление функции
        Function updateFunction = functionDAO.findId(id1);
        assertNotNull(updateFunction);

        updateFunction.setName("UpdatedFunction1");
        updateFunction.setType(FunctionType.SIN);
        functionDAO.update(updateFunction);

        Function updatedFunction = functionDAO.findId(id1);
        assertEquals("UpdatedFunction1", updatedFunction.getName());
        assertEquals(FunctionType.SIN, updatedFunction.getType());

        // Удаление функции
        functionDAO.delete(id2);
        Function deletedFunction = functionDAO.findId(id2);
        assertNull(deletedFunction);
    }

    @Test
    public void testFindCriteriaSortedWithMultipleFunctions() throws SQLException {
        // Создаём много функций с разными параметрами
        Function f1 = new Function(null, "Function1", FunctionType.SQR, LocalDateTime.now().minusDays(2), user1);
        Long id1 = functionDAO.create(f1);

        Function f2 = new Function(null, "Function2", FunctionType.IDENTITY, LocalDateTime.now().minusDays(1), user2);
        Long id2 = functionDAO.create(f2);

        Function f3 = new Function(null, "Function1", FunctionType.SIN, LocalDateTime.now().minusHours(12), user1);
        Long id3 = functionDAO.create(f3);

        Function f4 = new Function(null, "Function3", FunctionType.SQR, LocalDateTime.now().minusHours(6), user1);
        Long id4 = functionDAO.create(f4);

        Function f5 = new Function(null, "Function1", FunctionType.SQR, LocalDateTime.now(), user2);
        Long id5 = functionDAO.create(f5);

        // Поиск по имени "Alpha" сортировка по created_at ASC
        List<Function> resultsByName = functionDAO.findCriteriaSorted("Function1", null, null, "created_at", SortOrder.ASCENDING);
        assertEquals(3, resultsByName.size());
        assertTrue(resultsByName.stream().allMatch(f -> "Function1".equals(f.getName())));

        // Проверяем упорядоченность по created_at
        for (int i = 1; i < resultsByName.size(); ++i)
            assertTrue(!resultsByName.get(i).getCreatedAt().isBefore(resultsByName.get(i - 1).getCreatedAt()));

        // Поиск по типу SQR сортировка по name DESC
        List<Function> resultsByType = functionDAO.findCriteriaSorted(null, FunctionType.SQR, null, "name", SortOrder.DESCENDING);
        assertTrue(resultsByType.size() >= 2);
        assertTrue(resultsByType.stream().allMatch(f -> f.getType() == FunctionType.SQR));

        for (int i = 1; i < resultsByType.size(); ++i)
            assertTrue(resultsByType.get(i).getName().compareTo(resultsByType.get(i - 1).getName()) <= 0);

        // Поиск по ownerId = user1, сортировка по id ASC
        List<Function> resultsByOwner = functionDAO.findCriteriaSorted(null, null, user1, "id", SortOrder.ASCENDING);
        assertTrue(resultsByOwner.size() >= 3);
        assertTrue(resultsByOwner.stream().allMatch(f -> f.getOwnerId().equals(user1)));

        // Поиск с комбинированными фильтрами

        // Комбинация имя + тип
        List<Function> resultsCombined = functionDAO.findCriteriaSorted("Function1", FunctionType.SQR, null, "id", SortOrder.ASCENDING);
        assertEquals(2, resultsCombined.size());
        assertTrue(resultsCombined.stream().allMatch(f -> "Function1".equals(f.getName()) && f.getType() == FunctionType.SQR));

        // Комбинация тип + владелец
        List<Function> byTypeOwner = functionDAO.findCriteriaSorted(null, FunctionType.SQR, user1, "id", SortOrder.ASCENDING);
        assertEquals(2, byTypeOwner.size());
        assertTrue(byTypeOwner.stream().allMatch(f -> f.getType() == FunctionType.SQR && f.getOwnerId().equals(user1)));

        // Проверка пустого фильтра (возвращаем всё)
        List<Function> allFunctions = functionDAO.findCriteriaSorted(null, null, null, "id", SortOrder.ASCENDING);
        assertTrue(allFunctions.size() >= 5);
    }

    @AfterEach
    public void close() throws SQLException {
        logger.info("Закрываем соединение после каждого теста.");

        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM function");
            st.execute("DELETE FROM users");
        }

        if (connection != null && !connection.isClosed())
            connection.close();
    }
}