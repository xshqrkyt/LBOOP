package com.lab5.dao;

import com.lab5.enums.FunctionType;
import com.lab5.enums.UserRole;
import com.lab5.entity.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class PointsDAOTest {
    private Connection connection;

    private UserDAO userDAO;
    private FunctionDAO functionDAO;
    private PointsDAO pointsDAO;

    private Long userId;
    private Long functionId;

    private static final Logger logger = LogManager.getLogger(PointsDAOTest.class);

    @BeforeEach
    public void setup() throws SQLException {
        logger.info("Подключаемся к H2 с эмуляцией PostgreSQL.");

        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "", "");

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
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS function (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    type VARCHAR(50) NOT NULL CHECK (type IN ('SQR', 'IDENTITY', 'COMPOSITE', 'CONSTANT', 'NEWTON_METHOD', 'DEBOOR', 'SIN')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    owner_id BIGINT NOT NULL,
                    
                    CONSTRAINT fk_owner FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS points (
                    id BIGSERIAL PRIMARY KEY,
                    x_value DOUBLE ARRAY NOT NULL,
                    y_value DOUBLE ARRAY NOT NULL,
                    function_id BIGINT NOT NULL,
                    
                    CONSTRAINT fk_function FOREIGN KEY(function_id) REFERENCES function(id) ON DELETE CASCADE
                );
            """);
        }

        logger.info("Таблицы созданы.");

        userDAO = new UserDAO(connection);
        functionDAO = new FunctionDAO(connection);
        pointsDAO = new PointsDAO(connection);

        logger.info("Создаём пользователя для БД.");

        // Создаем пользователя
        User user = new User(null, "user3547", "12345", "test@example.com", UserRole.USER, LocalDateTime.now());
        userId = userDAO.create(user);
        assertNotNull(userId);

        logger.info("Создаём функцию для БД.");

        // Создаем функцию, принадлежащую пользователю
        Function function = new Function(null, "TestFunction", FunctionType.SQR, LocalDateTime.now(), userId);
        functionId = functionDAO.create(function);
        assertNotNull(functionId);
    }

    @Test
    void methodsTest() throws SQLException {
        logger.info("Тестируются создание в БД точек, поиск их по id, имени, изменение данных у них и удаление их из БД.");

        Double[] xValues = {1.0, 2.0, 3.0};
        Double[] yValues = {1.0, 4.0, 9.0};

        Points points = new Points(null, xValues, yValues, functionId);
        Long pointsId = pointsDAO.create(points);

        Points foundPoints = pointsDAO.findId(functionId);
        assertNotNull(foundPoints);
        assertArrayEquals(xValues, foundPoints.getXValues());
        assertArrayEquals(yValues, foundPoints.getYValues());
        assertEquals(functionId, foundPoints.getFunctionId());

        // Обновление значений
        Double[] newxValues = {4.0, 5.0, 6.0};
        Double[] newyValues = {16.0, 25.0, 36.0};
        foundPoints.setXValues(newxValues);
        foundPoints.setYValues(newyValues);

        pointsDAO.update(foundPoints);

        Points updatedPoints = pointsDAO.findId(functionId);
        assertNotNull(updatedPoints);
        assertArrayEquals(newxValues, updatedPoints.getXValues());
        assertArrayEquals(newyValues, updatedPoints.getYValues());

        // Удаление
        pointsDAO.delete(functionId);
        Points deletedPoints = pointsDAO.findId(functionId);
        assertNull(deletedPoints);
    }

    @Test
    public void findFunctionIdsTest() throws SQLException {
        // Создаём данные для первой функции
        Double[] x1 = {1.0, 2.0};
        Double[] y1 = {1.0, 4.0};
        Points p1 = new Points(null, x1, y1, functionId);
        pointsDAO.create(p1);

        // Создаём вторую функцию с точками
        Long functionId2 = functionDAO.create(new Function(null, "Func2", FunctionType.SIN, LocalDateTime.now(), userId));
        Double[] x2 = {3.0, 4.0};
        Double[] y2 = {9.0, 16.0};
        Points p2 = new Points(null, x2, y2, functionId2);
        pointsDAO.create(p2);

        // Тест: поиск по двум существующим functionId
        var foundPoints = pointsDAO.findFunctionIds(List.of(functionId, functionId2));
        assertEquals(2, foundPoints.size());
        assertTrue(foundPoints.stream().anyMatch(p -> p.getFunctionId().equals(functionId)));
        assertTrue(foundPoints.stream().anyMatch(p -> p.getFunctionId().equals(functionId2)));

        // Тест: поиск по пустому списку functionId (ожидаем пустой результат)
        foundPoints = pointsDAO.findFunctionIds(Collections.emptyList());
        assertTrue(foundPoints.isEmpty());

        // Тест: поиск по несуществующему functionId (ожидаем пустой результат)
        foundPoints = pointsDAO.findFunctionIds(List.of(-1L, -2L));
        assertTrue(foundPoints.isEmpty());

        // Тест: поиск с повторяющимися functionId
        foundPoints = pointsDAO.findFunctionIds(List.of(functionId, functionId, functionId2));
        assertEquals(2, foundPoints.size()); // Дубликаты functionId в запросе не влияют на количество результатов

        // Тест: поиск по одному functionId
        foundPoints = pointsDAO.findFunctionIds(List.of(functionId2));
        assertEquals(1, foundPoints.size());
        assertEquals(functionId2, foundPoints.get(0).getFunctionId());
    }

    @Test
    public void findFunctionIdSortedTest() throws SQLException {
        // Создаём несколько различных точек для одной функции
        Double[] x1 = {1.0, 2.0};
        Double[] y1 = {1.0, 4.0};
        Points p1 = new Points(null, x1, y1, functionId);
        pointsDAO.create(p1);

        Double[] x2 = {3.0, 4.0};
        Double[] y2 = {9.0, 16.0};
        Points p2 = new Points(null, x2, y2, functionId);
        pointsDAO.create(p2);

        Double[] x3 = {0.5, 1.5};
        Double[] y3 = {0.25, 2.25};
        Points p3 = new Points(null, x3, y3, functionId);
        pointsDAO.create(p3);

        // Сортировка по id ASC
        var pointsSortedByIdAsc = pointsDAO.findFunctionIdSorted(functionId, "id", SortOrder.ASCENDING);
        assertTrue(pointsSortedByIdAsc.size() >= 3);

        for (int i = 1; i < pointsSortedByIdAsc.size(); ++i)
            assertTrue(pointsSortedByIdAsc.get(i).getId() > pointsSortedByIdAsc.get(i - 1).getId());

        // Сортировка по id DESC
        var pointsSortedByIdDesc = pointsDAO.findFunctionIdSorted(functionId, "id", SortOrder.DESCENDING);
        assertTrue(pointsSortedByIdDesc.size() >= 3);

        for (int i = 1; i < pointsSortedByIdDesc.size(); ++i)
            assertTrue(pointsSortedByIdDesc.get(i).getId() < pointsSortedByIdDesc.get(i - 1).getId());

        // Сортировка по function_id ASC
        var pointsSortedByFunctionAsc = pointsDAO.findFunctionIdSorted(functionId, "function_id", SortOrder.ASCENDING);
        assertTrue(pointsSortedByFunctionAsc.size() >= 3);

        for (Points p : pointsSortedByFunctionAsc)
            assertEquals(functionId, p.getFunctionId());

        // Проверка, что метод выбросит исключение при неверном поле сортировки
        assertThrows(IllegalArgumentException.class, () -> pointsDAO.findFunctionIdSorted(functionId, "invalid_column", SortOrder.ASCENDING));
    }

    @Test
    public void findUserIdTest() throws SQLException {
        // Создаём несколько функций разного владельца с точками
        Long functionId1 = functionDAO.create(new Function(null, "F1", FunctionType.SQR, LocalDateTime.now(), userId));
        Long functionId2 = functionDAO.create(new Function(null, "F2", FunctionType.IDENTITY, LocalDateTime.now(), userId));

        Points points1 = new Points(null, new Double[]{0.0, 1.0}, new Double[]{0.0, 1.0}, functionId1);
        pointsDAO.create(points1);

        Points points2 = new Points(null, new Double[]{2.0, 3.0}, new Double[]{4.0, 9.0}, functionId2);
        pointsDAO.create(points2);

        // Запрос точек по userId
        var pointsForUser = pointsDAO.findUserId(userId);
        assertNotNull(pointsForUser);
        assertTrue(pointsForUser.size() >= 2);

        // Проверка, что все точки принадлежат функциям пользователя userId
        for (Points p : pointsForUser) {
            assertNotNull(p.getFunctionId());

            // Проверяем, что функция, которой принадлежат точки, действительно принадлежит userId
            Function f = functionDAO.findId(p.getFunctionId());
            assertNotNull(f);
            assertEquals(userId, f.getOwnerId());
        }

        // Тест на отсутствие точек для несуществующего пользователя
        Long fakeUserId = -1L;
        var emptyPointsList = pointsDAO.findUserId(fakeUserId);
        assertNotNull(emptyPointsList);
        assertTrue(emptyPointsList.isEmpty());

        // Тест с добавлением функции без точек для пользователя
        Long functionId3 = functionDAO.create(new Function(null, "F3", FunctionType.SIN, LocalDateTime.now(), userId));
        var pointsAfterNewFunction = pointsDAO.findUserId(userId);

        // Точек для functionId3 нет, количество точек не уменьшилось
        assertTrue(pointsAfterNewFunction.size() >= 2);
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