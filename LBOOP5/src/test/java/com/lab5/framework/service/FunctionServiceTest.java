// src/test/java/com/lab5/framework/service/FunctionServiceTest.java
package com.lab5.framework.service;

import com.lab5.common.dto.FunctionRequest;
import com.lab5.common.dto.FunctionResponse;
import com.lab5.common.dto.PointRequest;
import com.lab5.common.enums.UserRole;
import com.lab5.framework.entity.User;
import com.lab5.framework.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class FunctionServiceTest {

    @Autowired
    private FunctionService functionService;

    @Autowired
    private UserRepository userRepository;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("service_test_user");
        user.setPasswordHash("password123");
        user.setEmail("service_test@example.com");
        user.setRole(UserRole.USER);
        User savedUser = userRepository.save(user);
        testUserId = savedUser.getId();
    }

    @Test
    void testCreateFunction() {
        FunctionRequest request = new FunctionRequest();
        request.setName("Test Function");
        request.setType("TABULATED");

        PointRequest point1 = new PointRequest();
        point1.setX(1.0);
        point1.setY(2.0);

        PointRequest point2 = new PointRequest();
        point2.setX(3.0);
        point2.setY(6.0);

        request.setPoints(Arrays.asList(point1, point2));

        FunctionResponse response = functionService.createFunction(request, testUserId);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Test Function", response.getName());
        assertEquals("TABULATED", response.getType());
        assertEquals(testUserId, response.getOwnerId());
        assertEquals(2, response.getPoints().size());
        assertEquals(1.0, response.getPoints().get(0).getX());
        assertEquals(6.0, response.getPoints().get(1).getY());
    }

    @Test
    void testGetUserFunctions() {
        // Create multiple functions
        createTestFunction("Function A", "TABULATED", 2);
        createTestFunction("Function B", "SQR", 3);
        createTestFunction("Function C", "IDENTITY", 1);

        List<FunctionResponse> functions = functionService.getUserFunctions(testUserId);

        assertEquals(3, functions.size());
        assertTrue(functions.stream().anyMatch(f -> f.getName().equals("Function A")));
        assertTrue(functions.stream().anyMatch(f -> f.getName().equals("Function B")));
        assertTrue(functions.stream().anyMatch(f -> f.getName().equals("Function C")));

        // Verify points are loaded
        FunctionResponse functionB = functions.stream()
                .filter(f -> f.getName().equals("Function B"))
                .findFirst()
                .orElseThrow();
        assertEquals(3, functionB.getPoints().size());
    }

    @Test
    void testSearchFunctions() {
        createTestFunction("Linear Regression", "TABULATED", 2);
        createTestFunction("Quadratic Formula", "SQR", 2);
        createTestFunction("Other", "IDENTITY", 1);

        List<FunctionResponse> searchResults = functionService.searchFunctions("linear", testUserId);
        assertEquals(1, searchResults.size());
        assertEquals("Linear Regression", searchResults.get(0).getName());

        List<FunctionResponse> formulaResults = functionService.searchFunctions("formula", testUserId);
        assertEquals(1, formulaResults.size());
        assertEquals("Quadratic Formula", formulaResults.get(0).getName());
    }

    @Test
    void testGetUserFunctionsSortedByName() {
        createTestFunction("Charlie", "TABULATED", 1);
        createTestFunction("Alpha", "SQR", 1);
        createTestFunction("Beta", "IDENTITY", 1);

        List<FunctionResponse> functions = functionService.getUserFunctionsSortedByName(testUserId);

        assertEquals(3, functions.size());
        assertEquals("Alpha", functions.get(0).getName());
        assertEquals("Beta", functions.get(1).getName());
        assertEquals("Charlie", functions.get(2).getName());
    }

    @Test
    void testGetUserFunctionsSortedByDate() throws InterruptedException {
        createTestFunction("Old Function", "TABULATED", 1);
        Thread.sleep(10); // Ensure timestamp difference
        createTestFunction("New Function", "SQR", 1);

        List<FunctionResponse> functions = functionService.getUserFunctionsSortedByDate(testUserId);

        assertEquals(2, functions.size());
        assertEquals("New Function", functions.get(0).getName());
        assertEquals("Old Function", functions.get(1).getName());
    }

    @Test
    void testCreateFunctionWithoutPoints() {
        FunctionRequest request = new FunctionRequest();
        request.setName("Function Without Points");
        request.setType("IDENTITY");
        // No points provided

        FunctionResponse response = functionService.createFunction(request, testUserId);

        assertNotNull(response);
        assertEquals("Function Without Points", response.getName());
        assertTrue(response.getPoints().isEmpty());
    }

    private void createTestFunction(String name, String type, int pointCount) {
        FunctionRequest request = new FunctionRequest();
        request.setName(name);
        request.setType(type);

        PointRequest[] points = new PointRequest[pointCount];
        for (int i = 0; i < pointCount; i++) {
            PointRequest point = new PointRequest();
            point.setX((double) i);
            point.setY((double) i * 2);
            points[i] = point;
        }
        request.setPoints(Arrays.asList(points));

        functionService.createFunction(request, testUserId);
    }
}
