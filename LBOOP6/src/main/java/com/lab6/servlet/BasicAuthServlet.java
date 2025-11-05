package com.lab6.servlet;

import com.lab6.dao.*;
import com.lab6.entity.User;
import com.lab6.enums.UserRole; // enum с ролями ADMIN, USER

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;

public class BasicAuthServlet implements Filter {
    private UserDAO userDAO;
    private FunctionDAO functionDAO;
    private PointsDAO pointsDAO;
    private CompositeFunctionDAO compositeFunctionDAO;
    private CompositeFunctionLinkDAO compositeFunctionLinkDAO;

    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "123456789");
            userDAO = new UserDAO(connection);
            functionDAO = new FunctionDAO(connection);
            pointsDAO = new PointsDAO(connection);
            compositeFunctionDAO = new CompositeFunctionDAO(connection);
            compositeFunctionLinkDAO = new CompositeFunctionLinkDAO(connection);
        }

        catch (ClassNotFoundException | SQLException error) {
            logger.error("Ошибка инициализации BasicAuthServlet", error);
            throw new ServletException("Failed to initialize DAOs", error);
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
            return;
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);

        if (values.length != 2) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String username = values[0];
        String password = values[1];

        try {
            User user = userDAO.findUsername(username);
            if (user == null || !verifyPassword(password, user.getPasswordHash())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Проверяем роль пользователя (ADMIN или USER)
            if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.USER) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\":\"Access denied: invalid role\"}");
                return;
            }

            // Сохраняем аутентифицированного пользователя и его роль в атрибуты запроса
            request.setAttribute("authenticatedUser", user);
            request.setAttribute("userRole", user.getRole());

            // Продолжаем цепочку вызовов (отдаём управление дальше в сервлеты)
            chain.doFilter(req, res);
        }

        catch (SQLException error) {
            logger.error("Ошибка аутентификации", error);
            throw new ServletException("Database error in authentication", error);
        }
    }

    private boolean verifyPassword(String rawPassword, String storedHash) {
        return BCrypt.checkpw(rawPassword, storedHash);
    }

    @Override
    public void destroy() {

    }
}