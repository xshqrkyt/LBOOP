package com.lab5.repository;

import com.lab5.common.enums.*;
import com.lab5.entity.*;
import com.lab5.repository.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.util.List;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class PointRepositoryTest {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private Function function;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUsername("ownerUser");
        owner.setPasswordHash("pass");
        owner.setEmail("owner@example.com");
        owner.setRole(UserRole.USER);
        owner = userRepository.save(owner);

        function = new Function();
        function.setName("TestFunction");
        function.setType("TABULATED");
        function.setOwner(owner);
        function = functionRepository.save(function);
    }

    @Test
    void testSaveAndFindPointsByFunctionId() {
        Point p1 = new Point();
        p1.setXValue(1.0);
        p1.setYValue(2.0);
        p1.setFunction(function);

        Point p2 = new Point();
        p2.setXValue(3.0);
        p2.setYValue(4.0);
        p2.setFunction(function);

        pointRepository.saveAll(List.of(p1, p2));

        List<Point> points = pointRepository.findByFunctionId(function.getId());

        assertEquals(2, points.size());
        assertTrue(points.stream().anyMatch(p -> p.getXValue().equals(1.0)));
        assertTrue(points.stream().anyMatch(p -> p.getXValue().equals(3.0)));
    }

    @Test
    void testFindPointsByFunctionOwnerId() {
        Point p1 = new Point();
        p1.setXValue(5.0);
        p1.setYValue(6.0);
        p1.setFunction(function);

        pointRepository.save(p1);

        List<Point> points = pointRepository.findByFunctionOwnerId(owner.getId());
        assertFalse(points.isEmpty());
        assertEquals(owner.getId(), points.get(0).getFunction().getOwner().getId());
    }

    @Test
    void testFindPointsWithSorting() {
        Point p1 = new Point();
        p1.setXValue(10.0);
        p1.setYValue(20.0);
        p1.setFunction(function);

        Point p2 = new Point();
        p2.setXValue(5.0);
        p2.setYValue(10.0);
        p2.setFunction(function);

        pointRepository.saveAll(List.of(p1, p2));

        List<Point> sortedPoints = pointRepository.findByFunctionId(function.getId(), Sort.by("xValue").ascending());
        assertEquals(2, sortedPoints.size());
        assertEquals(5.0, sortedPoints.get(0).getXValue());
        assertEquals(10.0, sortedPoints.get(1).getXValue());
    }

    @Test
    void testFindPointsByFunctionOwnerIdWithSorting() {
        // Подготовка точек с разными xValue
        Point p1 = new Point();
        p1.setXValue(10.0);
        p1.setYValue(20.0);
        p1.setFunction(function);

        Point p2 = new Point();
        p2.setXValue(5.0);
        p2.setYValue(10.0);
        p2.setFunction(function);

        pointRepository.saveAll(List.of(p1, p2));

        // Выполнение поиска с сортировкой по xValue по id владельца функции
        List<Point> sortedPoints = pointRepository.findByFunctionOwnerId(owner.getId(), Sort.by("xValue").ascending());

        assertEquals(2, sortedPoints.size());
        assertEquals(5.0, sortedPoints.get(0).getXValue());
        assertEquals(10.0, sortedPoints.get(1).getXValue());
    }
}