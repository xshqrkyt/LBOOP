package com.lab7.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lab7.dao.UserDAO;
import com.lab7.dto.UserRequest;
import com.lab7.dto.UserResponse;
import com.lab7.entity.User;
import com.lab7.enums.UserRole;
import com.lab7.service.UserService;
import com.lab7.util.Database;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class UserServlet extends HttpServlet {
    private Connection connection;
    private UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        rebuildService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ensureService();
        }

        catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("{\"message\":\"База данных недоступна\"}");
            return;
        }

        User authenticatedUser = (User) req.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"message\":\"Unauthorized\"}");
            return;
        }

        if (!(authenticatedUser.getRole().equals(UserRole.ADMIN) || authenticatedUser.getRole().equals(UserRole.USER))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"message\":\"Access denied\"}");
            return;
        }

        String idParam = req.getParameter("id");
        String email = req.getParameter("email");
        String username = req.getParameter("username");

        resp.setContentType("application/json");

        try {
            if (idParam != null) {
                Long id = Long.parseLong(idParam);
                UserResponse user = userService.getUserById(id);

                if (user == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"message\":\"User not found\"}");
                    return;
                }

                resp.getWriter().write(objectMapper.writeValueAsString(user));
            }

            else if (email != null) {
                UserResponse user = userService.getUserByEmail(email);
                if (user == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"message\":\"User not found\"}");
                    return;
                }

                resp.getWriter().write(objectMapper.writeValueAsString(user));
            }

            else if (username != null) {
                UserResponse user = userService.getUserByUsername(username);
                if (user == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"message\":\"User not found\"}");
                    return;
                }

                resp.getWriter().write(objectMapper.writeValueAsString(user));
            }

            else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"message\":\"Specify id, email or username parameter\"}");
            }
        }

        catch (Exception error) {
            logger.error("Ошибка в doGet", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Server error\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User authenticatedUser = (User) req.getAttribute("authenticatedUser");
        UserRequest userRequest = parseRequest(req);

        if (userRequest == null || userRequest.getPasswordHash() == null || userRequest.getPasswordHash().isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"Укажите имя и пароль\"}");
            return;
        }

        if (userRequest.getUsername() == null || userRequest.getUsername().isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"Имя пользователя обязательно\"}");
            return;
        }

        String passwordError = validatePassword(userRequest.getPasswordHash(), userRequest.getConfirmPassword());
        if (passwordError != null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"" + passwordError + "\"}");
            return;
        }

        if (authenticatedUser == null) {
            // Публичная регистрация: неавторизованный пользователь может создать обычную учётную запись
            userRequest.setRole(UserRole.USER);
        }

        else if (!authenticatedUser.getRole().equals(UserRole.ADMIN)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"message\":\"Access denied\"}");
            return;
        }

        try {
            ensureService();
            UserResponse createdUser = userService.createUser(userRequest);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(createdUser));
        }

        catch (Exception error) {
            logger.error("Ошибка в doPost", error);
            String message = "Failed to create user";
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            java.sql.SQLException sqlEx = error instanceof java.sql.SQLException
                    ? (java.sql.SQLException) error
                    : error.getCause() instanceof java.sql.SQLException ? (java.sql.SQLException) error.getCause() : null;
            if (sqlEx != null && sqlEx.getSQLState() != null && sqlEx.getSQLState().startsWith("08")) {
                status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                message = "База данных недоступна. Проверьте контейнер PostgreSQL или переменные окружения DB_*";
            }

            else if (sqlEx != null && "42P01".equals(sqlEx.getSQLState())) {
                try {
                    rebuildService();
                } catch (Exception ignored) { }
                status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                message = "База данных готовится. Повторите попытку через пару секунд.";
            }

            else if (sqlEx != null && sqlEx.getSQLState() != null && sqlEx.getSQLState().startsWith("23")) {
                status = HttpServletResponse.SC_CONFLICT;
                message = "Пользователь с таким логином уже существует";
            }
            resp.setStatus(status);
            resp.getWriter().write("{\"message\":\"" + message + "\"}");
        }
    }

    private synchronized void rebuildService() throws ServletException {
        try {
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
            connection = Database.getConnection();
            new com.lab7.util.DatabaseInitializer().initialize(connection);
            UserDAO userDAO = new UserDAO(connection);
            userService = new UserService(userDAO);
        }

        catch (ClassNotFoundException error) {
            logger.error("Ошибка инициализации UserService", error);
            throw new ServletException("PostgreSQL Driver not found", error);
        }

        catch (SQLException error) {
            logger.error("Ошибка инициализации UserService", error);
            throw new ServletException("Не удалось инициализировать UserService", error);
        }
    }

    private void ensureService() throws SQLException, ServletException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            rebuildService();
        }
    }

    private String validatePassword(String rawPassword, String confirmPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return "Пароль не может быть пустым";
        }

        if (confirmPassword != null && !confirmPassword.equals(rawPassword)) {
            return "Пароли не совпадают";
        }

        if (rawPassword.length() < 8) {
            return "Пароль должен быть не короче 8 символов";
        }

        boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);
        boolean hasLetter = rawPassword.chars().anyMatch(Character::isLetter);
        boolean hasUpper = rawPassword.chars().anyMatch(Character::isUpperCase);
        if (!hasDigit || !hasLetter) {
            return "Пароль должен содержать хотя бы одну букву и одну цифру";
        }

        if (!hasUpper) {
            return "Добавьте хотя бы одну заглавную букву";
        }

        return null;
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ensureService();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("{\"message\":\"База данных недоступна\"}");
            return;
        }
        User authenticatedUser = (User) req.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"message\":\"Unauthorized\"}");
            return;
        }

        if (!authenticatedUser.getRole().equals(UserRole.ADMIN)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"message\":\"Access denied\"}");
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"User id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        UserRequest userRequest = parseRequest(req);

        try {
            UserResponse updatedUser = userService.updateUser(id, userRequest);
            if (updatedUser == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"User not found\"}");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(updatedUser));
        }

        catch (Exception error) {
            logger.error("Ошибка в doPut", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to update user\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User authenticatedUser = (User) req.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"message\":\"Unauthorized\"}");
            return;
        }

        if (!authenticatedUser.getRole().equals(UserRole.ADMIN)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"message\":\"Access denied\"}");
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"User id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        try {
            boolean deleted = userService.deleteUser(id);
            if (!deleted) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"User not found\"}");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

        catch (Exception error) {
            logger.error("Ошибка в doDelete", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to delete user\"}");
        }
    }

    // Вспомогательный метод для чтения JSON из запроса и десериализации в UserRequest
    private UserRequest parseRequest(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }

        return objectMapper.readValue(sb.toString(), UserRequest.class);
    }
}