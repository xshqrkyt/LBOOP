package com.lab7.dao;

import com.lab7.entity.*;
import com.lab7.enums.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PointsDAOTest {
    private Connection connection;

    private UserDAO userDAO;
    private FunctionDAO functionDAO;
    private PointsDAO pointsDAO;

    private Long userId;
    private Long functionId;

    private static final Logger logger = LogManager.getLogger(PointsDAOTest.class);

    @BeforeEach
    public void setup() throws SQLException, IOException {
        logger.info("Подключаемся к H2 с эмуляцией PostgreSQL.");

        String[] tables = {"src/main/resources/scripts/tables/users_table.sql", "src/main/resources/scripts/tables/function_table.sql", "src/main/resources/scripts/tables/points_table.sql"};
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "", "");

        try (var st = connection.createStatement()) {
            logger.info("Создаём таблицы.");

            for (String script : tables) {
                String sql = new String(Files.readAllBytes(Paths.get(script)));

                st.execute(sql);
            }
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
    void methodsTest() throws SQLException, IOException {
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
    public void findFunctionIdSortedTest() throws SQLException, IOException {
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
    public void findOwnerIdSortedTest() throws SQLException, IOException {
        // Создаём несколько функций для одного владельца с разными датами создания (для сортировки)
        Long funcId1 = functionDAO.create(new Function(null, "Function1", FunctionType.SQR, LocalDateTime.now().minusDays(3), userId));
        Long funcId2 = functionDAO.create(new Function(null, "Function2", FunctionType.SIN, LocalDateTime.now().minusDays(2), userId));
        Long funcId3 = functionDAO.create(new Function(null, "Function3", FunctionType.IDENTITY, LocalDateTime.now().minusDays(1), userId));

        // Создаём точки для этих функций
        pointsDAO.create(new Points(null, new Double[]{1.1, 1.2}, new Double[]{2.1, 2.2}, funcId1));
        pointsDAO.create(new Points(null, new Double[]{2.1, 2.2}, new Double[]{3.1, 3.2}, funcId2));
        pointsDAO.create(new Points(null, new Double[]{3.1, 3.2}, new Double[]{4.1, 4.2}, funcId3));

        // Проверка сортировки по полю id по возрастанию
        List<Points> pointsAsc = pointsDAO.findOwnerIdSorted(userId, "id", SortOrder.ASCENDING);
        assertNotNull(pointsAsc);
        assertTrue(pointsAsc.size() >= 3);
        for (int i = 1; i < pointsAsc.size(); ++i)
            assertTrue(pointsAsc.get(i).getId() > pointsAsc.get(i - 1).getId());

        // Проверка сортировки по полю id по убыванию
        List<Points> pointsDesc = pointsDAO.findOwnerIdSorted(userId, "id", SortOrder.DESCENDING);
        assertNotNull(pointsDesc);
        assertTrue(pointsDesc.size() >= 3);
        for (int i = 1; i < pointsDesc.size(); ++i)
            assertTrue(pointsDesc.get(i).getId() < pointsDesc.get(i - 1).getId());

        // Проверка сортировки по полю function_id по возрастанию
        List<Points> pointsByFuncAsc = pointsDAO.findOwnerIdSorted(userId, "function_id", SortOrder.ASCENDING);
        assertNotNull(pointsByFuncAsc);
        assertTrue(pointsByFuncAsc.size() >= 3);
        for (Points p : pointsByFuncAsc)
            assertEquals(userId, functionDAO.findId(p.getFunctionId()).getOwnerId());

        // Проверка выброса исключения при некорректном поле сортировки
        assertThrows(IllegalArgumentException.class, () -> pointsDAO.findOwnerIdSorted(userId, "invalid_column", SortOrder.ASCENDING));
    }

    @Test
    public void findUserIdTest() throws SQLException, IOException {
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
            st.execute("DELETE FROM functions");
            st.execute("DELETE FROM users");
        }

        if (connection != null && !connection.isClosed())
            connection.close();
    }
}