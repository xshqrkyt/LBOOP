// src/test/java/com/lab5/integration/ManualFrameworkComparisonTest.java
package com.lab5.integration;

import com.lab5.common.dto.FunctionRequest;
import com.lab5.common.dto.FunctionResponse;
import com.lab5.common.dto.PointRequest;
import com.lab5.framework.service.FunctionService as FrameworkFunctionService;
import com.lab5.manual.service.FunctionService as ManualFunctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class ManualFrameworkComparisonTest {

    private static final Logger logger = LoggerFactory.getLogger(ManualFrameworkComparisonTest.class);

    @Autowired
    private ManualFunctionService manualFunctionService;

    @Autowired
    private FrameworkFunctionService frameworkFunctionService;

    @Autowired
    private com.lab5.manual.repository.UserRepository manualUserRepository;

    @Autowired
    private com.lab5.framework.repository.UserRepository frameworkUserRepository;

    private Long manualUserId;
    private Long frameworkUserId;

    @BeforeEach
    void setUp() {
        // Create users for both implementations
        com.lab5.manual.entity.User manualUser = new com.lab5.manual.entity.User();
        manualUser.setUsername("manual_user");
        manualUser.setPasswordHash("password123");
        manualUser.setEmail("manual@example.com");
        manualUser.setRole(com.lab5.common.enums.UserRole.USER);
        manualUserId = manualUserRepository.create(manualUser);

        com.lab5.framework.entity.User frameworkUser = new com.lab5.framework.entity.User();
        frameworkUser.setUsername("framework_user");
        frameworkUser.setPasswordHash("password123");
        frameworkUser.setEmail("framework@example.com");
        frameworkUser.setRole(com.lab5.common.enums.UserRole.USER);
        com.lab5.framework.entity.User savedUser = frameworkUserRepository.save(frameworkUser);
        frameworkUserId = savedUser.getId();
    }

    @Test
    void testBothImplementationsProduceSimilarResults() {
        // Create identical function requests
        FunctionRequest request = createTestFunctionRequest();

        // Create function using manual implementation
        FunctionResponse manualResponse = manualFunctionService.createFunction(request, manualUserId);

        // Create function using framework implementation
        FunctionResponse frameworkResponse = frameworkFunctionService.createFunction(request, frameworkUserId);

        // Verify both implementations produce valid responses
        assertNotNull(manualResponse);
        assertNotNull(frameworkResponse);

        assertEquals(request.getName(), manualResponse.getName());
        assertEquals(request.getName(), frameworkResponse.getName());
        assertEquals(request.getType(), manualResponse.getType());
        assertEquals(request.getType(), frameworkResponse.getType());

        // Verify points
        assertEquals(request.getPoints().size(), manualResponse.getPoints().size());
        assertEquals(request.getPoints().size(), frameworkResponse.getPoints().size());

        // Verify point values
        for (int i = 0; i < request.getPoints().size(); i++) {
            PointRequest originalPoint = request.getPoints().get(i);
            assertEquals(originalPoint.getX(), manualResponse.getPoints().get(i).getX());
            assertEquals(originalPoint.getY(), manualResponse.getPoints().get(i).getY());
            assertEquals(originalPoint.getX(), frameworkResponse.getPoints().get(i).getX());
            assertEquals(originalPoint.getY(), frameworkResponse.getPoints().get(i).getY());
        }

        logger.info("Manual function ID: {}, Framework function ID: {}",
                manualResponse.getId(), frameworkResponse.getId());
    }

    @Test
    void testSearchFunctionalityBothImplementations() {
        // Create test data for both implementations
        FunctionRequest linearRequest = createLinearFunctionRequest();
        FunctionRequest quadraticRequest = createQuadraticFunctionRequest();

        manualFunctionService.createFunction(linearRequest, manualUserId);
        manualFunctionService.createFunction(quadraticRequest, manualUserId);

        frameworkFunctionService.createFunction(linearRequest, frameworkUserId);
        frameworkFunctionService.createFunction(quadraticRequest, frameworkUserId);

        // Test search in both implementations
        List<FunctionResponse> manualLinearResults = manualFunctionService.searchFunctions("Linear", manualUserId);
        List<FunctionResponse> frameworkLinearResults = frameworkFunctionService.searchFunctions("Linear", frameworkUserId);

        assertEquals(1, manualLinearResults.size());
        assertEquals(1, frameworkLinearResults.size());
        assertEquals("Linear Function", manualLinearResults.get(0).getName());
        assertEquals("Linear Function", frameworkLinearResults.get(0).getName());

        List<FunctionResponse> manualQuadraticResults = manualFunctionService.searchFunctions("Quadratic", manualUserId);
        List<FunctionResponse> frameworkQuadraticResults = frameworkFunctionService.searchFunctions("Quadratic", frameworkUserId);

        assertEquals(1, manualQuadraticResults.size());
        assertEquals(1, frameworkQuadraticResults.size());
        assertEquals("Quadratic Function", manualQuadraticResults.get(0).getName());
        assertEquals("Quadratic Function", frameworkQuadraticResults.get(0).getName());
    }

    private FunctionRequest createTestFunctionRequest() {
        FunctionRequest request = new FunctionRequest();
        request.setName("Test Function");
        request.setType("TABULATED");

        PointRequest point1 = new PointRequest();
        point1.setX(0.0);
        point1.setY(0.0);

        PointRequest point2 = new PointRequest();
        point2.setX(1.0);
        point2.setY(2.0);

        PointRequest point3 = new PointRequest();
        point3.setX(2.0);
        point3.setY(4.0);

        request.setPoints(Arrays.asList(point1, point2, point3));
        return request;
    }

    private FunctionRequest createLinearFunctionRequest() {
        FunctionRequest request = new FunctionRequest();
        request.setName("Linear Function");
        request.setType("TABULATED");

        PointRequest point1 = new PointRequest();
        point1.setX(0.0);
        point1.setY(0.0);

        PointRequest point2 = new PointRequest();
        point2.setX(1.0);
        point2.setY(1.0);

        request.setPoints(Arrays.asList(point1, point2));
        return request;
    }

    private FunctionRequest createQuadraticFunctionRequest() {
        FunctionRequest request = new FunctionRequest();
        request.setName("Quadratic Function");
        request.setType("SQR");

        PointRequest point1 = new PointRequest();
        point1.setX(0.0);
        point1.setY(0.0);

        PointRequest point2 = new PointRequest();
        point2.setX(1.0);
        point2.setY(1.0);

        PointRequest point3 = new PointRequest();
        point3.setX(2.0);
        point3.setY(4.0);

        request.setPoints(Arrays.asList(point1, point2, point3));
        return request;
    }
}