package com.lab5.dao;

import com.lab5.enums.UserRole;
import com.lab5.entity.User;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

import javax.swing.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserDAOTest {
    private Connection connection;
    private UserDAO dao;
    private Random random = new Random();

    private static final Logger logger = LogManager.getLogger(UserDAOTest.class);

    @BeforeEach
    public void setup() throws SQLException {
        logger.info("Подключаемся к H2 с эмуляцией PostgreSQL.");

        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "", "");

        try (var st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    email VARCHAR(100),
                    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);
        }

        logger.debug("Создана таблица.");

        dao = new UserDAO(connection);
    }

    private User createRandomUser() {
        logger.info("Создаём пользователя для БД.");

        String username = "user" + random.nextInt(10000);
        String passwordHash = "pass" + random.nextInt(10000);
        String email = username + "@example.com";
        UserRole role = UserRole.USER;

        logger.info("Пользователь создан.");
        return new User(null, username, passwordHash, email, role, LocalDateTime.now());
    }

    @Test
    public void methodsTest() throws SQLException {
        logger.info("Тестируются создание в БД пользователя, поиск его по id и имени, изменение данных у него и удаление его из БД.");

        User user = createRandomUser();
        Long id = dao.create(user);

        assertNotNull(id);

        // Поиск по id
        User newUser1 = dao.findId(id);

        assertNotNull(newUser1);
        assertEquals(user.getUsername(), newUser1.getUsername());
        assertEquals(user.getEmail(), newUser1.getEmail());
        assertEquals(user.getRole(), newUser1.getRole());

        // Поиск по имени
        User newUser2 = dao.findUsername(user.getUsername());

        assertNotNull(newUser2);
        assertEquals(id, newUser2.getId());
        assertEquals(user.getUsername(), newUser2.getUsername());
        assertEquals(user.getEmail(), newUser2.getEmail());
        assertEquals(user.getRole(), newUser2.getRole());

        // Обновление данных
        String newEmail = "changed@example.com";
        String newPassword = "12345";
        newUser1.setEmail(newEmail);
        newUser1.setPasswordHash(newPassword);
        newUser1.setRole(UserRole.ADMIN);
        dao.update(newUser1);

        User updated = dao.findId(id);
        assertEquals(newEmail, updated.getEmail());
        assertEquals(newPassword, updated.getPasswordHash());
        assertEquals(UserRole.ADMIN, updated.getRole());

        // Удаление
        dao.delete(id);
        User deleted = dao.findId(id);
        assertNull(deleted);
    }

    @Test
    public void notExistUserFind() throws SQLException {
        User newUser = dao.findUsername("error");

        assertNull(newUser);
    }

    @Test
    public void findCriteriaTest() throws SQLException {
        // Создаём множество пользователей с разными данными
        User user1 = createRandomUser(); // role USER
        dao.create(user1);

        User user2 = createRandomUser();
        user2.setRole(UserRole.ADMIN);
        dao.create(user2);

        User user3 = createRandomUser();
        user3.setEmail("special@example.com");
        dao.create(user3);

        User user4 = createRandomUser();
        user4.setEmail("special@example.com");
        user4.setRole(UserRole.ADMIN);
        dao.create(user4);

        User user5 = createRandomUser();
        dao.create(user5);

        User user6 = createRandomUser();
        user6.setEmail("special@example.com");
        user6.setRole(UserRole.USER);
        dao.create(user6);

        // Поиск по username
        var result1 = dao.findCriteria(user1.getUsername(), null, null);
        assertEquals(1, result1.size());
        assertTrue(result1.stream().allMatch(u -> u.getUsername().equals(user1.getUsername())));

        // Поиск по электронной почте (ожидаем 3 пользователя)
        var resultEmail = dao.findCriteria(null, "special@example.com", null);
        assertEquals(3, resultEmail.size());
        assertTrue(resultEmail.stream().allMatch(u -> "special@example.com".equals(u.getEmail())));

        // Поиск по роли ADMIN (ожидаем как минимум 2 пользователя (user2 и user4))
        var resultRole = dao.findCriteria(null, null, UserRole.ADMIN);
        assertTrue(resultRole.size() >= 2);
        assertTrue(resultRole.stream().allMatch(u -> u.getRole() == UserRole.ADMIN));

        // Поиск по email и роли ADMIN (ожидаем 1 пользователя (user4))
        var resultEmailRole = dao.findCriteria(null, "special@example.com", UserRole.ADMIN);
        assertEquals(1, resultEmailRole.size());
        assertTrue(resultEmailRole.stream().allMatch(u -> u.getEmail().equals("special@example.com") && u.getRole() == UserRole.ADMIN));

        // Поиск без фильтров, должно быть не меньше количества созданных пользователей
        var allUsers = dao.findCriteria(null, null, null);
        assertTrue(allUsers.size() >= 6);
    }

    @Test
    public void testFindByUsernameSorted() throws SQLException {
        // Создаём и сохраняем несколько пользователей
        User user1 = createRandomUser();
        Long id1 = dao.create(user1);

        User user2 = createRandomUser();
        Long id2 = dao.create(user2);

        // Поиск по username первого пользователя с сортировкой по id ASC
        User found1 = dao.findUsernameSorted(user1.getUsername(), "id", SortOrder.ASCENDING);
        assertNotNull(found1);
        assertEquals(user1.getUsername(), found1.getUsername());
        assertEquals(id1, found1.getId());

        // Поиск по username второго пользователя с сортировкой по email DESC
        User found2 = dao.findUsernameSorted(user2.getUsername(), "email", SortOrder.DESCENDING);
        assertNotNull(found2);
        assertEquals(user2.getUsername(), found2.getUsername());
        assertEquals(id2, found2.getId());

        // Проверяем, что оба пользователя в базе различаются
        assertNotEquals(found1.getId(), found2.getId());

        // Тест на исключение при некорректном поле сортировки
        assertThrows(IllegalArgumentException.class, () -> {
            dao.findUsernameSorted(user1.getUsername(), "invalid_column", SortOrder.ASCENDING);
        });
    }

    @AfterEach
    public void close() throws SQLException {
        logger.info("Закрываем соединение после каждого теста.");

        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM users");
        }

        if (!connection.isClosed())
            connection.close();
    }
}