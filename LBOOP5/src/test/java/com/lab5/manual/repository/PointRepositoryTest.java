// src/test/java/com/lab5/manual/repository/PointRepositoryTest.java
package com.lab5.manual.repository;

import com.lab5.manual.entity.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class PointRepositoryTest {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private FunctionRepository functionRepository;

    private Long testFunctionId;

    @BeforeEach
    void setUp() {
        // Create a test function first
        com.lab5.manual.entity.Function function = new com.lab5.manual.entity.Function();
        function.setName("Test Function");
        function.setType("TABULATED");
        function.setOwnerId(1L);
        testFunctionId = functionRepository.create(function);
    }

    @Test
    void testCreatePoint() {
        Point point = new Point();
        point.setXValue(1.0);
        point.setYValue(2.0);
        point.setFunctionId(testFunctionId);

        Long pointId = pointRepository.create(point);
        assertNotNull(pointId);
        assertTrue(pointId > 0);
    }

    @Test
    void testFindByFunctionId() {
        // Create test points
        Point point1 = new Point();
        point1.setXValue(1.0);
        point1.setYValue(1.0);
        point1.setFunctionId(testFunctionId);
        pointRepository.create(point1);

        Point point2 = new Point();
        point2.setXValue(2.0);
        point2.setYValue(4.0);
        point2.setFunctionId(testFunctionId);
        pointRepository.create(point2);

        List<Point> points = pointRepository.findByFunctionId(testFunctionId);
        assertEquals(2, points.size());
        assertEquals(1.0, points.get(0).getXValue());
        assertEquals(4.0, points.get(1).getYValue());
    }

    @Test
    void testFindById() {
        Point point = new Point();
        point.setXValue(5.0);
        point.setYValue(10.0);
        point.setFunctionId(testFunctionId);
        Long pointId = pointRepository.create(point);

        Point found = pointRepository.findById(pointId).orElse(null);
        assertNotNull(found);
        assertEquals(5.0, found.getXValue());
        assertEquals(10.0, found.getYValue());
        assertEquals(testFunctionId, found.getFunctionId());
    }

    @Test
    void testUpdatePoint() {
        Point point = new Point();
        point.setXValue(1.0);
        point.setYValue(2.0);
        point.setFunctionId(testFunctionId);
        Long pointId = pointRepository.create(point);
        point.setId(pointId);

        point.setXValue(3.0);
        point.setYValue(6.0);
        pointRepository.update(point);

        Point updated = pointRepository.findById(pointId).orElse(null);
        assertNotNull(updated);
        assertEquals(3.0, updated.getXValue());
        assertEquals(6.0, updated.getYValue());
    }

    @Test
    void testDeleteByFunctionId() {
        Point point = new Point();
        point.setXValue(1.0);
        point.setYValue(2.0);
        point.setFunctionId(testFunctionId);
        pointRepository.create(point);

        pointRepository.deleteByFunctionId(testFunctionId);
        List<Point> points = pointRepository.findByFunctionId(testFunctionId);
        assertTrue(points.isEmpty());
    }
}