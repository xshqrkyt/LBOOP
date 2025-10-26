package com.lab5;

import com.lab5.mapper.Mapper;
import com.lab5.enums.FunctionType;
import com.lab5.enums.UserRole;
import com.lab5.dto.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.lab5.entity.*;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapperTest {
    private static final Logger logger = LogManager.getLogger(MapperTest.class);

    @Test
    public void userRequestTest() {
        logger.info("Запускаются тесты.");

        UserRequest request = new UserRequest("user576", "12345", "user576@example.com", "ADMIN");

        User user = Mapper.toEntity(request);

        assertNull(user.getId()); // id пока нет
        assertEquals("user576", user.getUsername());
        assertNull(user.getPasswordHash()); // пока null (хеширование отдельно)
        assertEquals("user576@example.com", user.getEmail());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    public void userResponseTest() {
        User user = new User(7565444L, "user982", "123456789", "user982@example.com", UserRole.USER, LocalDateTime.now());

        UserResponse response = Mapper.toResponse(user);

        assertEquals(7565444L, response.getId());
        assertEquals("user982", response.getUsername());
        assertEquals(UserRole.USER, user.getRole());
        assertEquals("user982@example.com", response.getEmail());
        assertEquals("USER", response.getRole());
    }

    @Test
    public void functionRequestTest() {
        FunctionRequest request = new FunctionRequest("Composite_Function", "COMPOSITE", 64784L);

        Function function = Mapper.toEntity(request);

        assertNull(function.getId()); // id пока нет
        assertEquals("Composite_Function", function.getName());
        assertEquals(FunctionType.COMPOSITE, function.getType());
        assertEquals(64784L, function.getOwnerId());

        logger.info("Тесты завершились.");
    }

    @Test
    public void functionResponseTest() {
        Function function = new Function(326576L, "DeBoorFunction", FunctionType.DEBOOR, LocalDateTime.now(),856653L);

        FunctionResponse response = Mapper.toResponse(function);

        assertEquals(326576L, response.getId());
        assertEquals("DeBoorFunction", response.getName());
        assertEquals("DEBOOR", response.getType());
        assertNotNull(function.getCreatedAt());
        assertEquals(856653L, response.getOwnerId());
    }

    @Test
    public void pointsRequestTest() {
        PointsRequest request = new PointsRequest(new Double[] {1.0, 2.0, 3.0}, new Double[] {1.0, 4.0, 9.0}, 7653465L);

        Points points = Mapper.toEntity(request);

        assertNull(points.getId()); // id пока нет
        assertArrayEquals(new Double[] {1.0, 2.0, 3.0}, points.getXValues());
        assertArrayEquals(new Double[] {1.0, 4.0, 9.0}, points.getYValues());
        assertEquals(7653465L, points.getFunctionId());
    }

    @Test
    public void pointsResponseTest() {
        Points points = new Points(4567746L, new Double[] {4.0, 5.0, 6.0}, new Double[] {16.0, 25.0, 36.0}, 265376L);

        PointsResponse response = Mapper.toResponse(points);

        assertEquals(4567746L, response.getId());
        assertArrayEquals(new Double[] {4.0, 5.0, 6.0}, response.getXValues());
        assertArrayEquals(new Double[] {16.0, 25.0, 36.0}, response.getYValues());
        assertEquals(265376L, response.getFunctionId());
    }

    @Test
    public void compositeFunctionRequestTest() {
        CompositeFunctionRequest request = new CompositeFunctionRequest("CompositeFunction", 123L);

        CompositeFunction compositeFunction = Mapper.toEntity(request);

        assertEquals("CompositeFunction", compositeFunction.getName());
        assertEquals(123L, compositeFunction.getOwnerId());
    }

    @Test
    public void compositeFunctionResponseTest() {
        CompositeFunction compositeFunction = new CompositeFunction(57687L, "CompositeFunction", 123L, LocalDateTime.now());

        CompositeFunctionResponse response = Mapper.toResponse(compositeFunction);

        assertEquals(57687L, response.getId());
        assertEquals("CompositeFunction", response.getName());
        assertEquals(123L, response.getOwnerId());
    }

    @Test
    public void compositeFunctionLinkRequestTest() {
        CompositeFunctionLinkRequest request = new CompositeFunctionLinkRequest(10L, 20L, 5);

        CompositeFunctionLink compositeFunctionLink = Mapper.toEntity(request);

        assertEquals(10L, compositeFunctionLink.getCompositeId());
        assertEquals(20L, compositeFunctionLink.getFunctionId());
        assertEquals(5, compositeFunctionLink.getOrderIndex());
    }

    @Test
    public void compositeFunctionLinkResponseTest() {
        CompositeFunctionLink compositeFunctionLink = new CompositeFunctionLink(100L, 10L, 20L, 5);

        CompositeFunctionLinkResponse response = Mapper.toResponse(compositeFunctionLink);

        assertEquals(100L, response.getId());
        assertEquals(10L, response.getCompositeId());
        assertEquals(20L, response.getFunctionId());
        assertEquals(5, response.getOrderIndex());
    }
}