package com.lab7.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab7.dao.FunctionDAO;
import com.lab7.dao.PointsDAO;
import com.lab7.dto.PointsRequest;
import com.lab7.dto.PointsResponse;
import com.lab7.entity.User;
import com.lab7.enums.UserRole;
import com.lab7.service.FunctionService;
import com.lab7.service.PointsService;
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

public class PointsServlet extends HttpServlet {
    private PointsService pointsService;
    private FunctionService functionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(PointsServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Connection connection = Database.getConnection();
            FunctionDAO functionDAO = new FunctionDAO(connection);
            PointsDAO pointsDAO = new PointsDAO(connection);
            functionService = new FunctionService(functionDAO);
            pointsService = new PointsService(pointsDAO);
        }

        catch (ClassNotFoundException error) {
            logger.error("Ошибка инициализации PointsService", error);
            throw new ServletException("PostgreSQL Driver not found", error);
        }

        catch (SQLException error) {
            logger.error("Ошибка инициализации PointsService", error);
            throw new ServletException("Не удалось инициализировать PointsService", error);
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
        String functionIdParam = req.getParameter("functionId");
        String ownerIdParam = req.getParameter("ownerId");
        String userIdParam = req.getParameter("userId");
        String sortBy = req.getParameter("sortBy");
        String orderParam = req.getParameter("order");

        resp.setContentType("application/json");
        try {
            if (idParam != null) {
                Long id = Long.parseLong(idParam);
                PointsResponse points = pointsService.getPointsById(id);

                if (points == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"message\":\"Points not found\"}");
                    return;
                }
                if (!isAdmin) {
                    var function = functionService.getFunctionById(points.getFunctionId());
                    if (function == null) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"message\":\"Function not found\"}");
                        return;
                    }
                    if (!authenticatedUser.getId().equals(function.getOwnerId())) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write("{\"message\":\"Access denied\"}");
                        return;
                    }
                }
                resp.getWriter().write(objectMapper.writeValueAsString(points));
            }

            else if (functionIdParam != null) {
                Long functionId = Long.parseLong(functionIdParam);
                if (!isAdmin) {
                    var function = functionService.getFunctionById(functionId);
                    if (function == null) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"message\":\"Function not found\"}");
                        return;
                    }
                    if (!authenticatedUser.getId().equals(function.getOwnerId())) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write("{\"message\":\"Access denied\"}");
                        return;
                    }
                }
                boolean ascending = !"desc".equalsIgnoreCase(orderParam);
                List<PointsResponse> points = pointsService.getPointsByFunctionId(functionId, sortBy, ascending);
                resp.getWriter().write(objectMapper.writeValueAsString(points));
            }

            else if (ownerIdParam != null) {
                Long ownerId = Long.parseLong(ownerIdParam);
                if (!isAdmin && !authenticatedUser.getId().equals(ownerId)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }
                boolean ascending = !"desc".equalsIgnoreCase(orderParam);
                List<PointsResponse> points = pointsService.getPointsByOwnerId(ownerId, sortBy, ascending);
                resp.getWriter().write(objectMapper.writeValueAsString(points));
            }

            else if (userIdParam != null) {
                Long userId = Long.parseLong(userIdParam);
                if (!isAdmin && !authenticatedUser.getId().equals(userId)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }
                List<PointsResponse> points = pointsService.getPointsByUserId(userId);
                resp.getWriter().write(objectMapper.writeValueAsString(points));
            }

            else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"message\":\"Specify id or filtering parameters\"}");
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
        try {
            PointsRequest pointsRequest = parseRequest(req);
            boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);

            if (pointsRequest.getFunctionId() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"message\":\"Function id is required\"}");
                return;
            }

            var function = functionService.getFunctionById(pointsRequest.getFunctionId());
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

            PointsResponse createdPoints = pointsService.createPoints(pointsRequest);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(createdPoints));
        }

        catch (Exception error) {
            logger.error("Ошибка в doPost", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to create points\"}");
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
            resp.getWriter().write("{\"message\":\"Points id is required\"}");
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            PointsRequest pointsRequest = parseRequest(req);
            PointsResponse existing = pointsService.getPointsById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Points not found\"}");
                return;
            }

            boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
            Long targetFunctionId = pointsRequest.getFunctionId() != null ? pointsRequest.getFunctionId() : existing.getFunctionId();
            var function = functionService.getFunctionById(targetFunctionId);
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

            if (!isAdmin)
                pointsRequest.setFunctionId(targetFunctionId);

            PointsResponse updatedPoints = pointsService.updatePoints(id, pointsRequest);
            if (updatedPoints == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Points not found\"}");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(updatedPoints));
        }

        catch (Exception error) {
            logger.error("Ошибка в doPut", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to update points\"}");
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
            resp.getWriter().write("{\"message\":\"Points id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        try {
            PointsResponse existing = pointsService.getPointsById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Points not found\"}");
                return;
            }

            var function = functionService.getFunctionById(existing.getFunctionId());
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

            boolean deleted = pointsService.deletePoints(id);
            if (!deleted) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Points not found\"}");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

        catch (Exception error) {
            logger.error("Ошибка в doDelete", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to delete points\"}");
        }
    }

    private PointsRequest parseRequest(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }

        return objectMapper.readValue(sb.toString(), PointsRequest.class);
    }
}