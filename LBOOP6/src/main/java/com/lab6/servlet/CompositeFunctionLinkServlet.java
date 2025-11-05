package com.lab6.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab6.dto.CompositeFunctionLinkRequest;
import com.lab6.dto.CompositeFunctionLinkResponse;
import com.lab6.entity.User;
import com.lab6.enums.UserRole;
import com.lab6.service.CompositeFunctionLinkService;
import com.lab6.dao.CompositeFunctionLinkDAO;
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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

public class CompositeFunctionLinkServlet extends HttpServlet {
    private CompositeFunctionLinkService service;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(CompositeFunctionLinkServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "123456789");
            service = new CompositeFunctionLinkService(new CompositeFunctionLinkDAO(connection));
        }

        catch (ClassNotFoundException error) {
            logger.error("Ошибка инициализации CompositeFunctionLinkService", error);
            throw new ServletException("PostgreSQL Driver not found", error);
        }

        catch (SQLException error) {
            logger.error("Ошибка инициализации CompositeFunctionLinkService", error);
            throw new ServletException("Не удалось инициализировать CompositeFunctionLinkService", error);
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

        if (!(authenticatedUser.getRole().equals(UserRole.ADMIN) ||
                authenticatedUser.getRole().equals(UserRole.USER))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"message\":\"Access denied\"}");
            return;
        }

        String idParam = req.getParameter("id");
        String compositeIdParam = req.getParameter("compositeId");
        String functionIdParam = req.getParameter("functionId");
        String idsParam = req.getParameter("ids"); // comma separated ids
        String sortBy = req.getParameter("sortBy");
        String orderParam = req.getParameter("order");

        resp.setContentType("application/json");

        try {
            if (idParam != null) {
                Long id = Long.parseLong(idParam);
                CompositeFunctionLinkResponse link = service.getLinkById(id);

                if (link == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"message\":\"Link not found\"}");
                    return;
                }

                resp.getWriter().write(objectMapper.writeValueAsString(link));
            }

            else if (compositeIdParam != null) {
                Long compositeId = Long.parseLong(compositeIdParam);
                List<CompositeFunctionLinkResponse> links = service.getLinksByCompositeFunctionId(compositeId);
                resp.getWriter().write(objectMapper.writeValueAsString(links));
            }

            else if (functionIdParam != null) {
                Long functionId = Long.parseLong(functionIdParam);
                List<CompositeFunctionLinkResponse> links = service.getLinksByFunctionId(functionId);
                resp.getWriter().write(objectMapper.writeValueAsString(links));
            }

            else if (idsParam != null) {
                List<Long> ids = Arrays.stream(idsParam.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(Long::parseLong).collect(Collectors.toList());
                List<CompositeFunctionLinkResponse> links = service.getLinksByIds(ids);
                resp.getWriter().write(objectMapper.writeValueAsString(links));
            }

            else if (sortBy != null) {
                boolean ascending = !"desc".equalsIgnoreCase(orderParam);
                List<CompositeFunctionLinkResponse> links = service.getAllSorted(sortBy, ascending);
                resp.getWriter().write(objectMapper.writeValueAsString(links));
            }

            else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"message\":\"Specify id, compositeId, functionId, ids or sortBy parameter\"}");
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

        if (!authenticatedUser.getRole().equals(UserRole.ADMIN)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"message\":\"Access denied\"}");
            return;
        }

        CompositeFunctionLinkRequest request = parseRequest(req);
        try {
            CompositeFunctionLinkResponse created = service.createLink(request);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(created));
        }

        catch (Exception error) {
            logger.error("Ошибка в doPost", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to create link\"}");
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

        if (!authenticatedUser.getRole().equals(UserRole.ADMIN)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"message\":\"Access denied\"}");
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"Link id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        CompositeFunctionLinkRequest request = parseRequest(req);

        try {
            CompositeFunctionLinkResponse updated = service.updateLink(id, request);
            if (updated == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Link not found\"}");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(updated));
        }

        catch (Exception error) {
            logger.error("Ошибка в doPut", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to update link\"}");
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
            resp.getWriter().write("{\"message\":\"Link id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        try {
            boolean deleted = service.deleteLink(id);
            if (!deleted) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Link not found\"}");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

        catch (Exception error) {
            logger.error("Ошибка в doDelete", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Failed to delete link\"}");
        }
    }

    private CompositeFunctionLinkRequest parseRequest(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }

        return objectMapper.readValue(sb.toString(), CompositeFunctionLinkRequest.class);
    }
}