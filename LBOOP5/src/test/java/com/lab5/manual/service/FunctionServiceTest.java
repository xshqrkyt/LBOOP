// src/test/java/com/lab5/manual/service/FunctionServiceTest.java
package com.lab5.manual.service;

import com.lab5.common.dto.FunctionRequest;
import com.lab5.common.dto.FunctionResponse;
import com.lab5.common.dto.PointRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class FunctionServiceTest {

    @Autowired
    private FunctionService functionService;

    @Autowired
    private com.lab5.manual.repository.UserRepository userRepository;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        // Create test user
        com.lab5.manual.entity.User user = new com.lab5.manual.entity.User();
        user.setUsername("testuser_service");
        user.setPasswordHash("password123");
        user.setEmail("service@example.com");
        user.setRole(com.lab5.common.enums.UserRole.USER);
        testUserId = userRepository.create(user);
    }

    @Test
    void testCreateFunctionWithPoints() {
        FunctionRequest request = new FunctionRequest();
        request.setName("Linear Function");
        request.setType("TABULATED");

        PointRequest point1 = new PointRequest();
        point1.setX(1.0);
        point1.setY(1.0);

        PointRequest point2 = new PointRequest();
        point2.setX(2.0);
        point2.setY(2.0);

        PointRequest point3 = new PointRequest();
        point3.setX(3.0);
        point3.setY(3.0);

        request.setPoints(Arrays.asList(point1, point2, point3));

        FunctionResponse response = functionService.createFunction(request, testUserId);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Linear Function", response.getName());
        assertEquals("TABULATED", response.getType());
        assertEquals(testUserId, response.getOwnerId());
        assertEquals(3, response.getPoints().size());
        assertEquals(1.0, response.getPoints().get(0).getX());
        assertEquals(3.0, response.getPoints().get(2).getY());
    }

    @Test
    void testGetUserFunctions() {
        // Create test functions
        FunctionRequest request1 = new FunctionRequest();
        request1.setName("Function A");
        request1.setType("TABULATED");
        request1.setPoints(Arrays.asList(
                createPoint(1.0, 1.0),
                createPoint(2.0, 4.0)
        ));
        functionService.createFunction(request1, testUserId);

        FunctionRequest request2 = new FunctionRequest();
        request2.setName("Function B");
        request2.setType("SQR");
        request2.setPoints(Arrays.asList(
                createPoint(1.0, 1.0),
                createPoint(2.0, 4.0),
                createPoint(3.0, 9.0)
        ));
        functionService.createFunction(request2, testUserId);

        List<FunctionResponse> functions = functionService.getUserFunctions(testUserId);

        assertNotNull(functions);
        assertTrue(functions.size() >= 2);
        assertTrue(functions.stream().anyMatch(f -> f.getName().equals("Function A")));
        assertTrue(functions.stream().anyMatch(f -> f.getName().equals("Function B")));

        // Verify points are loaded
        FunctionResponse functionWithPoints = functions.stream()
                .filter(f -> f.getName().equals("Function B"))
                .findFirst()
                .orElseThrow();
        assertEquals(3, functionWithPoints.getPoints().size());
    }

    @Test
    void testSearchFunctions() {
        // Create test functions with specific names
        FunctionRequest request1 = new FunctionRequest();
        request1.setName("Linear Approximation");
        request1.setType("TABULATED");
        request1.setPoints(Arrays.asList(createPoint(1.0, 1.0)));
        functionService.createFunction(request1, testUserId);

        FunctionRequest request2 = new FunctionRequest();
        request2.setName("Quadratic Interpolation");
        request2.setType("SQR");
        request2.setPoints(Arrays.asList(createPoint(1.0, 1.0)));
        functionService.createFunction(request2, testUserId);

        FunctionRequest request3 = new FunctionRequest();
        request3.setName("Other Function");
        request3.setType("IDENTITY");
        request3.setPoints(Arrays.asList(createPoint(1.0, 1.0)));
        functionService.createFunction(request3, testUserId);

        // Search for "Linear"
        List<FunctionResponse> linearResults = functionService.searchFunctions("Linear", testUserId);
        assertEquals(1, linearResults.size());
        assertEquals("Linear Approximation", linearResults.get(0).getName());

        // Search for "Function"
        List<FunctionResponse> functionResults = functionService.searchFunctions("Function", testUserId);
        assertEquals(1, functionResults.size()); // Only "Other Function" should match
    }

    private PointRequest createPoint(double x, double y) {
        PointRequest point = new PointRequest();
        point.setX(x);
        point.setY(y);
        return point;
    }
}