package com.lab7.performance;

import com.lab7.dao.*;
import com.lab7.enums.*;
import com.lab7.entity.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.swing.*;

public class SortingPerformanceTest {
    private Connection connection;

    private UserDAO userDAO;
    private FunctionDAO functionDAO;
    private PointsDAO pointsDAO;
    CompositeFunctionDAO compositeFunctionDAO;
    CompositeFunctionLinkDAO compositeFunctionLinkDAO;

    private List<Long> userIds = new ArrayList<>();
    private List<Long> functionIds = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();
    private List<Long> compositeFunctionIds = new ArrayList<>();
    private List<String> compositeFunctionNames = new ArrayList<>();
    private List<Long> compositeFunctionLinkIds = new ArrayList<>();

    public SortingPerformanceTest(Connection connection) throws SQLException, IOException {
        this.connection = connection;
        this.userDAO = new UserDAO(connection);
        this.functionDAO = new FunctionDAO(connection);
        this.pointsDAO = new PointsDAO(connection);
        this.compositeFunctionDAO = new CompositeFunctionDAO(connection);
        this.compositeFunctionLinkDAO = new CompositeFunctionLinkDAO(connection);

        try (Statement st = connection.createStatement()) {
            String[] tables = {"scripts/tables/sorting_performance_table.sql", "scripts/tables/users_table.sql", "scripts/tables/function_table.sql", "scripts/tables/points_table.sql", "scripts/tables/composite_function_table.sql", "scripts/tables/composite_function_link_table.sql"};

            for (String script : tables) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(script);
                if (is == null)
                    throw new FileNotFoundException("Resource not found: " + script);

                String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                st.execute(sql);
            }
        }
    }

    public Set<String> generateUniqueNames(int count) {
        Set<String> names = new HashSet<>();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        while (names.size() < count) {
            int length = 8 + random.nextInt(5);
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++)
                sb.append(chars.charAt(random.nextInt(chars.length())));
            names.add(sb.toString());
        }
        return names;
    }

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

    public void runPerformanceTests(String branch) throws SQLException, IOException {
        Random random = new Random();

        // Создание пользователей (без замера времени)
        Set<String> usersSet = generateUniqueNames(10000);
        userNames.addAll(usersSet);
        for (int i = 0; i < userNames.size(); ++i) {
            Long id = userDAO.create(new User(null, userNames.get(i), "pass" + i, userNames.get(i) + "@example.com", UserRole.values()[random.nextInt(UserRole.values().length)], LocalDateTime.now()));
            userIds.add(id);
        }

        // Создание функций (без замера времени)
        for (int i = 0; i < userIds.size(); ++i) {
            Long id = functionDAO.create(new Function(null, "function" + i, FunctionType.values()[random.nextInt(FunctionType.values().length)], LocalDateTime.now(), userIds.get(i)));
            functionIds.add(id);
        }

        // Создание точек (без замера времени)
        for (int i = 0; i < functionIds.size(); ++i) {
            Double[] x = {random.nextDouble(), random.nextDouble(), random.nextDouble()};
            Double[] y = {random.nextDouble(), random.nextDouble(), random.nextDouble()};
            pointsDAO.create(new Points(null, x, y, functionIds.get(i)));
        }

        // Создаём сложных функции (без замера времени)
        for (int i = 0; i < 10000; ++i) {
            Long userId = userIds.get(random.nextInt(userIds.size()));
            String compName = "compFunc_" + i;
            Long id = compositeFunctionDAO.create(new CompositeFunction(null, compName, LocalDateTime.now(), userId));
            compositeFunctionIds.add(id);
            compositeFunctionNames.add(compName);
        }

        // Создаём ссылок на функции (без замера времени)
        for (int i = 0; i < 10000; ++i) {
            Long compositeId = compositeFunctionIds.get(random.nextInt(compositeFunctionIds.size()));
            Long functionId = functionIds.get(random.nextInt(functionIds.size()));
            Long id = compositeFunctionLinkDAO.create(new CompositeFunctionLink(null, compositeId, functionId, i));
            compositeFunctionLinkIds.add(id);
        }

        // Замер сортировок UserDAO
        measureAndRecord(branch, "findUsernameSorted(UserDAO ASC)", () -> {
            try {
                for (String username : userNames)
                    userDAO.findUsernameSorted(username, "username", SortOrder.ASCENDING);
            }

            catch (SQLException error) {
                throw new RuntimeException(error);
            }
        });

        measureAndRecord(branch, "findUsernameSorted(UserDAO DESC)", () -> {
            try {
                for (String username : userNames)
                    userDAO.findUsernameSorted(username, "username", SortOrder.DESCENDING);
            }

            catch (SQLException error) {
                throw new RuntimeException(error);
            }
        });

        // Замер сортировок FunctionDAO
        measureAndRecord(branch, "findCriteriaSorted(FunctionDAO ASC)", () -> {
            try {
                for (Long uid : userIds)
                    functionDAO.findCriteriaSorted(null, null, uid, "name", SortOrder.ASCENDING);
            }

            catch (SQLException error) {
                throw new RuntimeException(error);
            }
        });

        measureAndRecord(branch, "findCriteriaSorted(FunctionDAO DESC)", () -> {
            try {
                for (Long uid : userIds)
                    functionDAO.findCriteriaSorted(null, null, uid, "name", SortOrder.DESCENDING);
            }

            catch (SQLException error) {
                throw new RuntimeException(error);
            }
        });

        // Замер сортировок PointsDAO
        measureAndRecord(branch, "findFunctionIdSorted(PointsDAO ASC)", () -> {
            try {
                for (Long fid : functionIds)
                    pointsDAO.findFunctionIdSorted(fid, "id", SortOrder.ASCENDING);
            }

            catch (SQLException error) {
                throw new RuntimeException(error);
            }
        });

        measureAndRecord(branch, "findFunctionIdSorted(PointsDAO DESC)", () -> {
            try {
                for (Long fid : functionIds)
                    pointsDAO.findFunctionIdSorted(fid, "id", SortOrder.DESCENDING);
            }

            catch (SQLException error) {
                throw new RuntimeException(error);
            }
        });

        // Замер сортировок CompositeFunctionDAO
        measureAndRecord(branch, "findAllSorted(CompositeFunctionDAO ASC)", () -> {
            try {
                List<CompositeFunction> results = compositeFunctionDAO.findAllSorted(compositeFunctionNames, "name", SortOrder.ASCENDING);
            }

            catch (SQLException error) {
                throw new RuntimeException(error);
            }
        });

        measureAndRecord(branch, "findAllSorted(CompositeFunctionDAO ASC)", () -> {
            try {
                List<CompositeFunction> results = compositeFunctionDAO.findAllSorted(compositeFunctionNames, "name", SortOrder.DESCENDING);
            }

            catch (SQLException error) {
                throw new RuntimeException(error);
            }
        });

        // Замер сортировок CompositeFunctionLinkDAO
        measureAndRecord(branch, "findAllSorted(CompositeFunctionLinkDAO ASC)", () -> {
            try {
                List<CompositeFunctionLink> results = compositeFunctionLinkDAO.findAllSorted("order_index", SortOrder.ASCENDING);
            }

            catch (SQLException error) {
                throw new RuntimeException(error);
            }
        });

        measureAndRecord(branch, "findAllSorted(CompositeFunctionLinkDAO DESC)", () -> {
            try {
                List<CompositeFunctionLink> results = compositeFunctionLinkDAO.findAllSorted("order_index", SortOrder.DESCENDING);
            }

            catch (SQLException error) {
                throw new RuntimeException(error);
            }
        });
    }

    public void exportResultsToCsv(String path) throws SQLException, IOException {
        String query = "SELECT branch, query, execution_time_ms FROM query_performance";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery();
             FileWriter writer = new FileWriter(path)) {

            writer.write("branch,query,execution time (ms)\n");
            while (rs.next()) {
                writer.write(rs.getString("branch") + ",");
                writer.write(rs.getString("query") + ",");
                writer.write(rs.getLong("execution_time_ms") + "\n");
            }
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")) {
            SortingPerformanceTest test = new SortingPerformanceTest(conn);
            test.runPerformanceTests("manual");
            test.exportResultsToCsv("sorting_performance_results.csv");

            System.out.println("Результаты готовы.");
        }
    }
}