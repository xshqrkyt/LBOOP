package com.lab7.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab7.dto.*;
import com.lab7.entity.User;
import com.lab7.enums.FunctionType;
import com.lab7.enums.UserRole;
import com.lab7.functions.TabulatedFunction;
import com.lab7.functions.factory.ArrayTabulatedFunctionFactory;
import com.lab7.functions.factory.LinkedListTabulatedFunctionFactory;
import com.lab7.functions.factory.TabulatedFunctionFactory;
import com.lab7.operations.TabulatedDifferentialOperator;
import com.lab7.operations.TabulatedFunctionOperationService;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OperationServlet extends HttpServlet {
    private FunctionService functionService;
    private PointsService pointsService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(OperationServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Connection connection = Database.getConnection();
            functionService = new FunctionService(new com.lab7.dao.FunctionDAO(connection));
            pointsService = new PointsService(new com.lab7.dao.PointsDAO(connection));
        }

        catch (ClassNotFoundException error) {
            logger.error("PostgreSQL driver not found", error);
            throw new ServletException("Driver not found", error);
        }

        catch (SQLException error) {
            logger.error("Failed to init OperationServlet", error);
            throw new ServletException("Cannot init OperationServlet", error);
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

        boolean isAdmin = authenticatedUser.getRole() == UserRole.ADMIN;
        OperationRequest request = parseRequest(req);
        if (request.getOperation() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"operation is required\"}");
            return;
        }

        TabulatedFunctionFactory factory = selectFactory(request.getFactory());
        TabulatedFunctionOperationService operationService = new TabulatedFunctionOperationService(factory);
        TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator(factory);

        resp.setContentType("application/json");

        try {
            String op = request.getOperation().toLowerCase();
            switch (op) {
                case "add":
                case "sub":
                case "mul":
                case "div": {
                    TabulatedFunction f1 = loadOwnedFunction(request.getFirstFunctionId(), authenticatedUser, isAdmin, factory);
                    TabulatedFunction f2 = loadOwnedFunction(request.getSecondFunctionId(), authenticatedUser, isAdmin, factory);
                    TabulatedFunction result = switch (op) {
                        case "add" -> operationService.add(f1, f2);
                        case "sub" -> operationService.subtract(f1, f2);
                        case "mul" -> operationService.multiply(f1, f2);
                        default -> operationService.divide(f1, f2);
                    };
                    OperationResponse response = persistResult(result, request, authenticatedUser, factory, "Результат операции сохранён");
                    resp.getWriter().write(objectMapper.writeValueAsString(response));
                    break;
                }
                case "diff": {
                    TabulatedFunction source = loadOwnedFunction(request.getFirstFunctionId(), authenticatedUser, isAdmin, factory);
                    TabulatedFunction derivative = differentialOperator.derive(source);
                    OperationResponse response = persistResult(derivative, request, authenticatedUser, factory, "Производная сохранена");
                    resp.getWriter().write(objectMapper.writeValueAsString(response));
                    break;
                }
                case "apply": {
                    if (request.getApplyX() == null)
                        throw new IllegalArgumentException("applyX is required for apply operation");
                    TabulatedFunction source = loadOwnedFunction(request.getFirstFunctionId(), authenticatedUser, isAdmin, factory);
                    double value = source.apply(request.getApplyX());
                    OperationResponse response = new OperationResponse(null, null, value, null, "Значение вычислено");
                    resp.getWriter().write(objectMapper.writeValueAsString(response));
                    break;
                }
                case "integral": {
                    TabulatedFunction source = loadOwnedFunction(request.getFirstFunctionId(), authenticatedUser, isAdmin, factory);
                    int threads = request.getThreads() != null && request.getThreads() > 0 ? Math.min(request.getThreads(), 16) : 1;
                    double integral = computeIntegral(source, threads);
                    OperationResponse response = new OperationResponse(null, null, integral, null, "Интеграл вычислен");
                    resp.getWriter().write(objectMapper.writeValueAsString(response));
                    break;
                }
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"message\":\"Unknown operation\"}");
            }
        }

        catch (IllegalArgumentException error) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(new OperationResponse(null, null, null, null, error.getMessage())));
        }

        catch (Exception error) {
            logger.error("Ошибка в OperationServlet", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(objectMapper.writeValueAsString(new OperationResponse(null, null, null, null, "Server error: " + error.getMessage())));
        }
    }

    private OperationRequest parseRequest(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }
        if (sb.isEmpty())
            return new OperationRequest();
        return objectMapper.readValue(sb.toString(), OperationRequest.class);
    }

    private TabulatedFunction loadOwnedFunction(Long id, User user, boolean isAdmin, TabulatedFunctionFactory factory) throws Exception {
        if (id == null)
            throw new IllegalArgumentException("functionId is required");
        FunctionResponse function = functionService.getFunctionById(id);
        if (function == null)
            throw new IllegalArgumentException("Function not found: " + id);
        if (!isAdmin && !user.getId().equals(function.getOwnerId()))
            throw new IllegalArgumentException("Access denied to function " + id);

        List<PointsResponse> points = pointsService.getPointsByFunctionId(id, null, true);
        if (points.isEmpty())
            throw new IllegalArgumentException("Нет точек для функции " + id);
        return toTabulated(points.get(0), factory);
    }

    private TabulatedFunction toTabulated(PointsResponse points, TabulatedFunctionFactory factory) {
        Double[] xs = points.getXValues();
        Double[] ys = points.getYValues();
        if (xs == null || ys == null || xs.length != ys.length || xs.length < 2)
            throw new IllegalArgumentException("Некорректные точки функции");

        List<double[]> sorted = IntStream.range(0, xs.length)
                .mapToObj(i -> new double[]{xs[i], ys[i]})
                .sorted(Comparator.comparingDouble(a -> a[0]))
                .collect(Collectors.toList());

        double[] xValues = sorted.stream().mapToDouble(a -> a[0]).toArray();
        double[] yValues = sorted.stream().mapToDouble(a -> a[1]).toArray();
        return factory.create(xValues, yValues);
    }

    private TabulatedFunctionFactory selectFactory(String name) {
        if ("linked_list".equalsIgnoreCase(name) || "linkedlist".equalsIgnoreCase(name))
            return new LinkedListTabulatedFunctionFactory();
        return new ArrayTabulatedFunctionFactory();
    }

    private OperationResponse persistResult(TabulatedFunction function, OperationRequest request, User user, TabulatedFunctionFactory factory, String message) throws Exception {
        int count = function.getCount();
        Double[] xValues = new Double[count];
        Double[] yValues = new Double[count];
        for (int i = 0; i < count; i++) {
            xValues[i] = function.getX(i);
            yValues[i] = function.getY(i);
        }

        if (request.getResultName() == null || request.getResultName().isBlank())
            return new OperationResponse(xValues, yValues, null, null, message);

        FunctionRequest newFunction = new FunctionRequest(request.getResultName(), FunctionType.TABULATED.name(), user.getId());
        FunctionResponse saved = functionService.createFunction(newFunction);
        PointsRequest pointsRequest = new PointsRequest(xValues, yValues, saved.getId());
        pointsService.createPoints(pointsRequest);

        return new OperationResponse(xValues, yValues, null, saved.getId(), message);
    }

    private double computeIntegral(TabulatedFunction function, int threads) {
        int segments = function.getCount() - 1;
        if (segments < 1)
            throw new IllegalArgumentException("Недостаточно точек для интеграла");

        ensureAscending(function);
        if (threads <= 1)
            return trapezoidal(function, 0, segments);

        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            return pool.submit(() -> {
                DoubleAdder sum = new DoubleAdder();
                IntStream.range(0, segments).parallel().forEach(i -> sum.add(segmentArea(function, i)));
                return sum.doubleValue();
            }).get();
        }
        catch (Exception error) {
            throw new RuntimeException("Не удалось вычислить интеграл", error);
        }
        finally {
            pool.shutdown();
        }
    }

    private void ensureAscending(TabulatedFunction function) {
        for (int i = 1; i < function.getCount(); i++) {
            if (function.getX(i) <= function.getX(i - 1))
                throw new IllegalArgumentException("X должен быть строго возрастающим");
        }
    }

    private double trapezoidal(TabulatedFunction function, int start, int segments) {
        double sum = 0.0;
        for (int i = start; i < start + segments; i++)
            sum += segmentArea(function, i);
        return sum;
    }

    private double segmentArea(TabulatedFunction function, int i) {
        double dx = function.getX(i + 1) - function.getX(i);
        return (function.getY(i) + function.getY(i + 1)) * dx / 2.0;
    }
}
