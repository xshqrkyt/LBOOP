package com.lab7.performance;

import com.lab7.dao.*;
import com.lab7.enums.*;
import com.lab7.entity.*;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.io.*;

public class PerformanceTest {
    Connection connection;

    UserDAO userDAO;
    FunctionDAO functionDAO;
    PointsDAO pointsDAO;
    CompositeFunctionDAO compositeFunctionDAO;
    CompositeFunctionLinkDAO compositeFunctionLinkDAO;

    private List<Long> userIds = new ArrayList<>();
    private List<Long> functionIds = new ArrayList<>();

    private List<String> userNames = new ArrayList<>();
    private List<String> functionNames = new ArrayList<>();
    private List<Long> compositeFunctionIds = new ArrayList<>();
    private List<Long> compositeFunctionLinkIds = new ArrayList<>();

    public PerformanceTest(Connection connection) throws SQLException, IOException {
        this.connection = connection;
        this.userDAO = new UserDAO(connection);
        this.functionDAO = new FunctionDAO(connection);
        this.pointsDAO = new PointsDAO(connection);
        this.compositeFunctionDAO = new CompositeFunctionDAO(connection);
        this.compositeFunctionLinkDAO = new CompositeFunctionLinkDAO(connection);

        String[] tables = {"scripts/tables/performance_table.sql", "scripts/tables/users_table.sql", "scripts/tables/function_table.sql", "scripts/tables/points_table.sql", "scripts/tables/composite_function_table.sql", "scripts/tables/composite_function_link_table.sql"};

        try (Statement st = connection.createStatement()) {
            for (String script : tables) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(script);
                if (is == null)
                    throw new FileNotFoundException("Resource not found: " + script);

                String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                st.execute(sql);
            }
        }
    }

    // Генератор уникальных случайных имён
    public Set<String> generateUniqueNames(int count) {
        Set<String> names = new HashSet<>();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        while (names.size() < count) {
            int length = 8 + random.nextInt(5);
            StringBuilder name = new StringBuilder(length);

            for (int i = 0; i < length; ++i)
                name.append(chars.charAt(random.nextInt(chars.length())));

            names.add(name.toString());
        }

        return names;
    }

    // Измерение и запись времени выполнения 10000 запросов
    public void measureAndRecord(String branch, String queryName, Runnable task) throws SQLException {
        long start = System.nanoTime();
        task.run();
        long durationMs = (System.nanoTime() - start) / 1000000;

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO query_performance (branch, query, execution_time_ms) VALUES (?, ?, ?)")) {
            ps.setString(1, branch);
            ps.setString(2, queryName);
            ps.setLong(3, durationMs);
            ps.executeUpdate();
        }
    }

    // Запуск тестов для методов DAO
    public void runTests(String branch) throws SQLException {
        Random random = new Random();

        // Создание пользователей
        Set<String> userNamesSet = generateUniqueNames(10000);
        userNames = new ArrayList<>(userNamesSet);
        userIds = new ArrayList<>();

        measureAndRecord(branch, "create(UserDAO)", () -> {
            for (int i = 1; i <= 10000; ++i) {
                try {
                    Long userId = userDAO.create(new User(null, userNames.get(i - 1), "pass" + i, userNames.get(i - 1) + "@example.com", UserRole.values()[random.nextInt(UserRole.values().length)], LocalDateTime.now()));
                    userIds.add(userId);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Создание функций
        Set<String> functionNamesSet = generateUniqueNames(10000);
        functionNames = new ArrayList<>(functionNamesSet);
        functionIds = new ArrayList<>();

        measureAndRecord(branch, "create(FunctionDAO)", () -> {
            for (int i = 1; i <= 10000; ++i) {
                try {
                    Long funcId = functionDAO.create(new Function(null, "func" + i, FunctionType.values()[random.nextInt(FunctionType.values().length)], LocalDateTime.now(), userIds.get(i - 1)));
                    functionIds.add(funcId);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Создание точек
        measureAndRecord(branch, "create(PointsDAO)", () -> {
            for (int i = 1; i <= 10000; ++i) {
                try {
                    Double[] xVals = {random.nextDouble(), random.nextDouble(), random.nextDouble()};
                    Double[] yVals = {random.nextDouble(), random.nextDouble(), random.nextDouble()};
                    pointsDAO.create(new Points(null, xVals, yVals, functionIds.get(i - 1)));
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Создание сложных функций
        measureAndRecord(branch, "create(CompositeFunctionDAO)", () -> {
            for (int i = 1; i <= 10000; ++i) {
                try {
                    Long userId = userIds.get(random.nextInt(userIds.size()));
                    Long id = compositeFunctionDAO.create(new CompositeFunction(null, "compFunc" + i, LocalDateTime.now(), userId));
                    compositeFunctionIds.add(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Создание ссылок между функциями (CompositeFunctionLink)
        measureAndRecord(branch, "create(CompositeFunctionLinkDAO)", () -> {
            for (int i = 1; i <= 10000; ++i) {
                try {
                    Long compositeId = compositeFunctionIds.get(random.nextInt(compositeFunctionIds.size()));
                    Long functionId = functionIds.get(random.nextInt(functionIds.size()));
                    Long id = compositeFunctionLinkDAO.create(new CompositeFunctionLink(null, compositeId, functionId, i));
                    compositeFunctionLinkIds.add(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Поиск пользователей
        measureAndRecord(branch, "findId(UserDAO)", () -> {
            for (Long id : userIds) {
                try {
                    userDAO.findId(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        measureAndRecord(branch, "findUsername(UserDAO)", () -> {
            for (String name : userNames) {
                try {
                    userDAO.findUsername(name);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Поиск функций
        measureAndRecord(branch, "findId(FunctionDAO)", () -> {
            for (Long id : functionIds) {
                try {
                    functionDAO.findId(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        measureAndRecord(branch, "findName(FunctionDAO)", () -> {
            for (String name : functionNames) {
                try {
                    functionDAO.findName(name);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        measureAndRecord(branch, "findOwnerId(FunctionDAO)", () -> {
            for (Long id : userIds) {
                try {
                    functionDAO.findOwnerId(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        measureAndRecord(branch, "findType(FunctionDAO)", () -> {
            for (FunctionType type : FunctionType.values()) {
                try {
                    functionDAO.findType(type);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Поиск точек
        measureAndRecord(branch, "findId(PointsDAO)", () -> {
            for (Long id : functionIds) {
                try {
                    pointsDAO.findId(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Поиск сложных функций по id
        measureAndRecord(branch, "findId(CompositeFunctionDAO)", () -> {
            for (Long id : compositeFunctionIds) {
                try {
                    compositeFunctionDAO.findId(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Поиск ссылок по id
        measureAndRecord(branch, "findId(CompositeFunctionLinkDAO)", () -> {
            for (Long id : compositeFunctionLinkIds) {
                try {
                    compositeFunctionLinkDAO.findId(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Поиск ссылок по compositeFunctionId
        measureAndRecord(branch, "findByFunctionId(CompositeFunctionLinkDAO)", () -> {
            for (Long id : functionIds) {
                try {
                    compositeFunctionLinkDAO.findByFunctionId(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Поиск ссылок по compositeFunctionId
        measureAndRecord(branch, "findByCompositeFunctionId(CompositeFunctionLinkDAO)", () -> {
            for (Long id : compositeFunctionIds) {
                try {
                    compositeFunctionLinkDAO.findByCompositeFunctionId(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Обновление пользователей
        measureAndRecord(branch, "update(UserDAO)", () -> {
            for (int i = 1; i <= 10000; ++i) {
                try {
                    User user = userDAO.findId(userIds.get(i - 1));
                    if (user != null) {
                        user.setEmail("updated" + i + "@example.com");
                        userDAO.update(user);
                    }
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Обновление функций
        measureAndRecord(branch, "update(FunctionDAO)", () -> {
            for (int i = 1; i <= 10000; ++i) {
                try {
                    Function func = functionDAO.findId(functionIds.get(i - 1));
                    if (func != null) {
                        func.setName(func.getName() + "_upd");
                        functionDAO.update(func);
                    }
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Обновление точек
        measureAndRecord(branch, "update(PointsDAO)", () -> {
            for (int i = 0; i < 10000; ++i) {
                try {
                    Points pts = pointsDAO.findId(functionIds.get(i));

                    if (pts != null) {
                        pts.setXValues(new Double[]{random.nextDouble(), random.nextDouble()});
                        pts.setYValues(new Double[]{random.nextDouble(), random.nextDouble()});

                        pointsDAO.update(pts);
                    }
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Обновление CompositeFunction
        measureAndRecord(branch, "update(CompositeFunctionDAO)", () -> {
            for (Long id : compositeFunctionIds) {
                try {
                    CompositeFunction compFunc = compositeFunctionDAO.findId(id);
                    if (compFunc != null) {
                        compFunc.setName(compFunc.getName() + "_upd");
                        compositeFunctionDAO.update(compFunc);
                    }
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Обновление CompositeFunctionLink
        measureAndRecord(branch, "update(CompositeFunctionLinkDAO)", () -> {
            for (Long id : compositeFunctionLinkIds) {
                try {
                    CompositeFunctionLink link = compositeFunctionLinkDAO.findId(id);
                    if (link != null) {
                        link.setOrderIndex(link.getOrderIndex() + 1);
                        compositeFunctionLinkDAO.update(link);
                    }
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Удаление пользователей
        measureAndRecord(branch, "delete(UserDAO)", () -> {
            for (Long id : userIds) {
                try {
                    userDAO.delete(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Удаление функций
        measureAndRecord(branch, "delete(FunctionDAO)", () -> {
            for (Long id : functionIds) {
                try {
                    functionDAO.delete(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Удаление точек
        measureAndRecord(branch, "delete(PointsDAO)", () -> {
            for (Long id : functionIds) {
                try {
                    pointsDAO.delete(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Удаление ссылок
        measureAndRecord(branch, "delete(CompositeFunctionLinkDAO)", () -> {
            for (Long id : compositeFunctionLinkIds) {
                try {
                    compositeFunctionLinkDAO.delete(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });

        // Удаление сложных функций
        measureAndRecord(branch, "delete(CompositeFunctionDAO)", () -> {
            for (Long id : compositeFunctionIds) {
                try {
                    compositeFunctionDAO.delete(id);
                }

                catch (SQLException | IOException error) {
                    throw new RuntimeException(error);
                }
            }
        });
    }

    // Экспорт результата в CSV
    public void exportResult(String filepath) throws SQLException, IOException {
        String query = "SELECT branch, query, execution_time_ms FROM query_performance;";

        try (PreparedStatement ps = connection.prepareStatement(query); ResultSet rs = ps.executeQuery(); FileWriter writer = new FileWriter(filepath, true)) {
            writer.append("branch,query,execution time (ms)\n");

            while (rs.next()) {
                writer.append(rs.getString("branch")).append(",");
                writer.append(rs.getString("query")).append(",");
                writer.append(String.valueOf(rs.getLong("execution_time_ms")));
                writer.append("\n");
            }
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        try (Connection connect = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "", "")) {
            PerformanceTest pt = new PerformanceTest(connect);
            pt.runTests("manual");
            pt.exportResult("performance_results.csv");

            System.out.println("Результаты готовы.");
        }
    }
}