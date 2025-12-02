package com.lab7.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab7.dao.CompositeFunctionDAO;
import com.lab7.dao.CompositeFunctionLinkDAO;
import com.lab7.dao.FunctionDAO;
import com.lab7.dto.CompositeFunctionLinkRequest;
import com.lab7.dto.CompositeFunctionLinkResponse;
import com.lab7.entity.User;
import com.lab7.enums.UserRole;
import com.lab7.service.CompositeFunctionService;
import com.lab7.service.CompositeFunctionLinkService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeFunctionLinkServlet extends HttpServlet {
    private CompositeFunctionLinkService service;
    private CompositeFunctionService compositeFunctionService;
    private FunctionService functionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(CompositeFunctionLinkServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Connection connection = Database.getConnection();
            service = new CompositeFunctionLinkService(new CompositeFunctionLinkDAO(connection));
            compositeFunctionService = new CompositeFunctionService(new CompositeFunctionDAO(connection));
            functionService = new FunctionService(new FunctionDAO(connection));
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

        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        if (!(isAdmin || authenticatedUser.getRole().equals(UserRole.USER))) {
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

                if (!isAdmin && !ownsComposite(link.getCompositeId(), authenticatedUser)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }

                resp.getWriter().write(objectMapper.writeValueAsString(link));
            }

            else if (compositeIdParam != null) {
                Long compositeId = Long.parseLong(compositeIdParam);
                if (!isAdmin && !ownsComposite(compositeId, authenticatedUser)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }
                List<CompositeFunctionLinkResponse> links = service.getLinksByCompositeFunctionId(compositeId);
                resp.getWriter().write(objectMapper.writeValueAsString(links));
            }

            else if (functionIdParam != null) {
                Long functionId = Long.parseLong(functionIdParam);
                if (!isAdmin && !ownsFunction(functionId, authenticatedUser)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }
                List<CompositeFunctionLinkResponse> links = service.getLinksByFunctionId(functionId);
                if (!isAdmin)
                    links = links.stream().filter(link -> ownsComposite(link.getCompositeId(), authenticatedUser)).collect(Collectors.toList());
                resp.getWriter().write(objectMapper.writeValueAsString(links));
            }

            else if (idsParam != null) {
                List<Long> ids = Arrays.stream(idsParam.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(Long::parseLong).collect(Collectors.toList());
                List<CompositeFunctionLinkResponse> links = service.getLinksByIds(ids);
                if (!isAdmin)
                    links = links.stream().filter(link -> ownsComposite(link.getCompositeId(), authenticatedUser)).collect(Collectors.toList());
                resp.getWriter().write(objectMapper.writeValueAsString(links));
            }

            else if (sortBy != null) {
                boolean ascending = !"desc".equalsIgnoreCase(orderParam);
                List<CompositeFunctionLinkResponse> links = service.getAllSorted(sortBy, ascending);
                if (!isAdmin)
                    links = links.stream().filter(link -> ownsComposite(link.getCompositeId(), authenticatedUser)).collect(Collectors.toList());
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

        CompositeFunctionLinkRequest request = parseRequest(req);
        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        if (request.getCompositeId() == null || request.getFunctionId() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"CompositeId and functionId are required\"}");
            return;
        }

        if (!isAdmin) {
            if (!ownsComposite(request.getCompositeId(), authenticatedUser) || !ownsFunction(request.getFunctionId(), authenticatedUser)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"message\":\"Access denied\"}");
                return;
            }
        }
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

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"Link id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        CompositeFunctionLinkRequest request = parseRequest(req);

        try {
            CompositeFunctionLinkResponse existing = service.getLinkById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Link not found\"}");
                return;
            }

            boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
            Long targetCompositeId = request.getCompositeId() != null ? request.getCompositeId() : existing.getCompositeId();
            Long targetFunctionId = request.getFunctionId() != null ? request.getFunctionId() : existing.getFunctionId();

            if (!isAdmin) {
                if (!ownsComposite(targetCompositeId, authenticatedUser) || !ownsFunction(targetFunctionId, authenticatedUser)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"message\":\"Access denied\"}");
                    return;
                }
                request.setCompositeId(targetCompositeId);
                request.setFunctionId(targetFunctionId);
            }

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

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"Link id is required\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        try {
            CompositeFunctionLinkResponse existing = service.getLinkById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\":\"Link not found\"}");
                return;
            }

            if (!isAdmin && !ownsComposite(existing.getCompositeId(), authenticatedUser)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"message\":\"Access denied\"}");
                return;
            }

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

    private boolean ownsComposite(Long compositeId, User user) {
        if (compositeId == null)
            return false;

        try {
            var composite = compositeFunctionService.getCompositeFunctionById(compositeId);
            return composite != null && user.getId().equals(composite.getOwnerId());
        }

        catch (Exception error) {
            logger.error("Failed to verify composite ownership", error);
            return false;
        }
    }

    private boolean ownsFunction(Long functionId, User user) {
        if (functionId == null)
            return false;

        try {
            var function = functionService.getFunctionById(functionId);
            return function != null && user.getId().equals(function.getOwnerId());
        }

        catch (Exception error) {
            logger.error("Failed to verify function ownership", error);
            return false;
        }
    }
}