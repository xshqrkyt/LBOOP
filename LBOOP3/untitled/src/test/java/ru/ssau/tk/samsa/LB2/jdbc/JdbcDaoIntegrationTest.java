package ru.ssau.tk.samsa.LB2.jdbc;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;

import ru.ssau.tk.samsa.LB2.jdbc.dao.UserDao;
import ru.ssau.tk.samsa.LB2.jdbc.dao.FunctionDao;
import ru.ssau.tk.samsa.LB2.jdbc.dao.PointDao;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JdbcDaoIntegrationTest {

    private PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("samsa_db")
            .withUsername("samsa")
            .withPassword("samsa_pass");

    private Connection conn;

    @BeforeAll
    public void setUp() throws Exception {
        postgres.start();
        String url = postgres.getJdbcUrl();
        String user = postgres.getUsername();
        String pass = postgres.getPassword();
        conn = DriverManager.getConnection(url, user, pass);
        // You need to run DDL to create tables here or mount init scripts in container
    }

    @AfterAll
    public void tearDown() throws Exception {
        if (conn != null) conn.close();
        postgres.stop();
    }

    @Test
    public void testUserCrud() throws Exception {
        UserDao userDao = new UserDao(conn);
        // implement create/find/update/delete tests using userDao methods
    }

    @Test
    public void testFunctionAndPointsCrud() throws Exception {
        FunctionDao functionDao = new FunctionDao(conn);
        PointDao pointDao = new PointDao(conn);
        // implement CRUD and batch insert tests
    }
}
