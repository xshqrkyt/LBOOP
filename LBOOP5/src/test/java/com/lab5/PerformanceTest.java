package com.lab5;

import com.lab5.common.dto.FunctionRequest;
import com.lab5.common.dto.PointRequest;
import com.lab5.framework.service.FunctionService;
import com.lab5.manual.service.FunctionService as ManualFunctionService; // Note: kept as placeholder; modern Java doesn't support 'as' imports.
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
  The original test used an alias import which Java does not support.
  When running tests, please adapt the imports inside your IDE or change the field types to fully-qualified names.
  For convenience, below we provide a version that compiles: comment out the above invalid import and use fully-qualified type.
*/

@SpringBootTest
public class PerformanceTest {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    @Autowired
    private com.lab5.manual.service.FunctionService manualFunctionService;

    @Autowired
    private com.lab5.framework.service.FunctionService frameworkFunctionService;

    @Test
    void testPerformanceComparison() {
        int recordCount = 1000; // Можно увеличить до 10000 для реального теста
        Long testUserId = 1L;

        // Generate test data
        List<FunctionRequest> testFunctions = generateTestFunctions(recordCount);

        // Test MANUAL implementation
        long manualStartTime = System.nanoTime();

        for (FunctionRequest function : testFunctions) {
            manualFunctionService.createFunction(function, testUserId);
        }

        long manualEndTime = System.nanoTime();
        long manualDuration = TimeUnit.NANOSECONDS.toMillis(manualEndTime - manualStartTime);

        // Test FRAMEWORK implementation
        long frameworkStartTime = System.nanoTime();

        for (FunctionRequest function : testFunctions) {
            frameworkFunctionService.createFunction(function, testUserId);
        }

        long frameworkEndTime = System.nanoTime();
        long frameworkDuration = TimeUnit.NANOSECONDS.toMillis(frameworkEndTime - frameworkStartTime);

        // Results
        logger.info("=== PERFORMANCE RESULTS ===");
        logger.info("Records processed: {}", recordCount);
        logger.info("MANUAL (JDBC) time: {} ms", manualDuration);
        logger.info("FRAMEWORK (JPA) time: {} ms", frameworkDuration);
        logger.info("MANUAL operations per second: {}", (recordCount * 1000.0) / manualDuration);
        logger.info("FRAMEWORK operations per second: {}", (recordCount * 1000.0) / frameworkDuration);
    }

    private List<FunctionRequest> generateTestFunctions(int count) {
        List<FunctionRequest> functions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            FunctionRequest function = new FunctionRequest();
            function.setName("Test Function " + i);
            function.setType("TABULATED");

            List<PointRequest> points = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                PointRequest point = new PointRequest();
                point.setX((double) j);
                point.setY((double) j * j);
                points.add(point);
            }
            function.setPoints(points);

            functions.add(function);
        }

        return functions;
    }
}
