package com.lab7.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab7.dao.CompositeFunctionDAO;
import com.lab7.dto.CompositeFunctionRequest;
import com.lab7.dto.CompositeFunctionResponse;
import com.lab7.entity.User;
import com.lab7.enums.UserRole;
import com.lab7.service.CompositeFunctionService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeFunctionServlet extends HttpServlet {
    private CompositeFunctionService compositeFunctionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(CompositeFunctionServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Connection connection = Database.getConnection();
            CompositeFunctionDAO dao = new CompositeFunctionDAO(connection);
            compositeFunctionService = new CompositeFunctionService(dao);
        }

        catch (ClassNotFoundException error) {
            logger.error("Ошибка инициализации CompositeFunctionService", error);
            throw new ServletException("PostgreSQL Driver not found", error);
        }

        catch (SQLException error) {
            logger.error("Ошибка инициализации CompositeFunctionService", error);
            throw new ServletException("Не удалось инициализировать CompositeFunctionService", error);
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
        String namesParam = req.getParameter("names"); // comma separated names
        String ownerIdParam = req.getParameter("ownerId");
        String sortBy = req.getParameter("sortBy");
        String orderParam = req.getParameter("order");

        resp.setContentType("application/json");

        try {
            if (idParam != null) {
                Long id = Long.parseLong(idParam);
                CompositeFunctionResponse compFunction = compositeFunctionService.getCompositeFunctionById(id);

                if (compFunction == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"message\":\"Composite function not found\"}");
                    return;
                }

                if (!isAdmin && !authenticatedUser.getId().equals(compFunction.getOwnerId())) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }

                resp.getWriter().write(objectMapper.writeValueAsString(compFunction));
            }

            else if (name != null) {
                List<CompositeFunctionResponse> list = compositeFunctionService.getCompositeFunctionsByName(name);
                if (!isAdmin)
                    list = list.stream().filter(f -> authenticatedUser.getId().equals(f.getOwnerId())).collect(Collectors.toList());
                resp.getWriter().write(objectMapper.writeValueAsString(list));
            }

            else if (namesParam != null) {
                List<String> names = Arrays.stream(namesParam.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                boolean ascending = !"desc".equalsIgnoreCase(orderParam);
                List<CompositeFunctionResponse> list = compositeFunctionService.getCompositeFunctionsByNamesSorted(names, sortBy, ascending);
                if (!isAdmin)
                    list = list.stream().filter(f -> authenticatedUser.getId().equals(f.getOwnerId())).collect(Collectors.toList());
                resp.getWriter().write(objectMapper.writeValueAsString(list));
            }

            else if (ownerIdParam != null) {
                Long ownerId = Long.parseLong(ownerIdParam);
                if (!isAdmin && !authenticatedUser.getId().equals(ownerId)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }
                List<CompositeFunctionResponse> list = compositeFunctionService.getCompositeFunctionsByOwnerId(ownerId);
                resp.getWriter().write(objectMapper.writeValueAsString(list));
            }

            else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"message\":\"Specify id, name, names or ownerId parameter\"}");
            }
        }

        catch (Exception error) {
            logger.error("Ошибка в doGet", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Internal server error\"}");
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

        CompositeFunctionRequest request = parseRequest(req);
        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        if (!isAdmin)
            request.setOwnerId(authenticatedUser.getId());
        try {
            CompositeFunctionResponse created = compositeFunctionService.createCompositeFunction(request);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(created));
        }

        catch (Exception error) {
            logger.error("Ошибка в doPost", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to create composite function\"}");
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
            resp.getWriter().write("{\"message\":\"Composite function id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        CompositeFunctionRequest request = parseRequest(req);

        try {
            CompositeFunctionResponse existing = compositeFunctionService.getCompositeFunctionById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Composite function not found\"}");
                return;
            }

            boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
            if (!isAdmin) {
                if (!authenticatedUser.getId().equals(existing.getOwnerId())) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }
                request.setOwnerId(existing.getOwnerId());
            }

            CompositeFunctionResponse updated = compositeFunctionService.updateCompositeFunction(id, request);
            if (updated == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Composite function not found\"}");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(updated));
        }

        catch (Exception error) {
            logger.error("Ошибка в doPut", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to update composite function\"}");
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
            resp.getWriter().write("{\"message\":\"Composite function id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        try {
            CompositeFunctionResponse existing = compositeFunctionService.getCompositeFunctionById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Composite function not found\"}");
                return;
            }

            if (!isAdmin && !authenticatedUser.getId().equals(existing.getOwnerId())) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"message\":\"Access denied\"}");
                return;
            }

            boolean deleted = compositeFunctionService.deleteCompositeFunction(id);
            if (!deleted) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Composite function not found\"}");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

        catch (Exception error) {
            logger.error("Ошибка в doDelete", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to delete composite function\"}");
        }
    }

    private CompositeFunctionRequest parseRequest(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }

        return objectMapper.readValue(sb.toString(), CompositeFunctionRequest.class);
    }
}