package com.lab7.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lab7.dao.FunctionDAO;
import com.lab7.dto.FunctionRequest;
import com.lab7.dto.FunctionResponse;
import com.lab7.entity.User;
import com.lab7.enums.UserRole;
import com.lab7.service.FunctionService;
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
import java.util.List;
import java.util.stream.Collectors;

public class FunctionServlet extends HttpServlet {
    private FunctionService functionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(FunctionServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        try {
            Connection connection = Database.getConnection();
            FunctionDAO functionDAO = new FunctionDAO(connection);
            functionService = new FunctionService(functionDAO);
        }

        catch (ClassNotFoundException error) {
            logger.error("Ошибка инициализации FunctionService", error);
            throw new ServletException("PostgreSQL Driver not found", error);
        }

        catch (SQLException error) {
            logger.error("Ошибка инициализации FunctionService", error);
            throw new ServletException("Не удалось инициализировать FunctionService", error);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User authenticatedUser = (User) req.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"message\":\"Unauthorized\"}");
            return;
        }

        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        if (!(isAdmin || authenticatedUser.getRole().equals(UserRole.USER))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"message\":\"Access denied\"}");
            return;
        }

        String idParam = req.getParameter("id");
        String name = req.getParameter("name");
        String type = req.getParameter("type");
        String ownerIdParam = req.getParameter("ownerId");
        String sortBy = req.getParameter("sortBy");
        String orderParam = req.getParameter("order");

        resp.setContentType("application/json");
        try {
            if (idParam != null) {
                Long id = Long.parseLong(idParam);
                FunctionResponse function = functionService.getFunctionById(id);

                if (function == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"message\":\"Function not found\"}");
                    return;
                }

                if (!isAdmin && !authenticatedUser.getId().equals(function.getOwnerId())) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }

                resp.getWriter().write(objectMapper.writeValueAsString(function));
            }

            else if (name != null || type != null || ownerIdParam != null) {
                Long ownerId = null;
                if (ownerIdParam != null && !ownerIdParam.isEmpty()) {
                    ownerId = Long.parseLong(ownerIdParam);
                    if (!isAdmin && !authenticatedUser.getId().equals(ownerId)) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write("{\"message\":\"Access denied\"}");
                        return;
                    }
                }

                if (!isAdmin && ownerId == null)
                    ownerId = authenticatedUser.getId();

                boolean ascending = !"desc".equalsIgnoreCase(orderParam);
                List<FunctionResponse> functions = functionService.findFunctionsByCriteria(name, type, ownerId, sortBy, ascending);
                if (!isAdmin)
                    functions = functions.stream().filter(f -> authenticatedUser.getId().equals(f.getOwnerId())).collect(Collectors.toList());
                resp.getWriter().write(objectMapper.writeValueAsString(functions));
            }

            else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"message\":\"Specify at least id or some filtering parameter\"}");
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
        if (authenticatedUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"message\":\"Unauthorized\"}");
            return;
        }

        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);

        FunctionRequest functionRequest = parseRequest(req);
        if (!isAdmin)
            functionRequest.setOwnerId(authenticatedUser.getId());
        try {
            FunctionResponse createdFunction = functionService.createFunction(functionRequest);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(createdFunction));
        }

        catch (Exception error) {
            logger.error("Ошибка в doPost", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to create function\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User authenticatedUser = (User) req.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"message\":\"Unauthorized\"}");
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"Function id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        FunctionRequest functionRequest = parseRequest(req);
        try {
            FunctionResponse existing = functionService.getFunctionById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Function not found\"}");
                return;
            }

            boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
            if (!isAdmin) {
                if (!authenticatedUser.getId().equals(existing.getOwnerId())) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }
                functionRequest.setOwnerId(existing.getOwnerId());
            }

            FunctionResponse updatedFunction = functionService.updateFunction(id, functionRequest);
            if (updatedFunction == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Function not found\"}");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(updatedFunction));
        }

        catch (Exception error) {
            logger.error("Ошибка в doPut", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to update function\"}");
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

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"Function id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        try {
            FunctionResponse existing = functionService.getFunctionById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Function not found\"}");
                return;
            }

            if (!isAdmin && !authenticatedUser.getId().equals(existing.getOwnerId())) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"message\":\"Access denied\"}");
                return;
            }

            boolean deleted = functionService.deleteFunction(id);
            if (!deleted) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Function not found\"}");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

        catch (Exception error) {
            logger.error("Ошибка в doDelete", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to delete function\"}");
        }
    }

    private FunctionRequest parseRequest(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }

        return objectMapper.readValue(sb.toString(), FunctionRequest.class);
    }
}