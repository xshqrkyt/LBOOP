package com.lab7.repository;

import com.lab7.entity.Function;
import com.lab7.entity.Point;
import com.lab7.entity.User;
import com.lab7.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

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
    void testSaveAndFindPointByFunctionId() {
        Point point = new Point();
        point.setXValue(new double[]{1.0, 2.0});
        point.setYValue(new double[]{3.0, 4.0});
        point.setFunction(function);

        pointRepository.save(point);

        Point found = pointRepository.findByFunctionId(function.getId());
        assertNotNull(found);
        assertArrayEquals(new double[]{1.0, 2.0}, found.getXValue());
    }

    @Test
    void testFindPointByFunctionOwnerId() {
        Point point = new Point();
        point.setXValue(new double[]{5.0});
        point.setYValue(new double[]{6.0});
        point.setFunction(function);

        pointRepository.save(point);

        Point found = pointRepository.findByFunctionOwnerId(Long.valueOf(owner.getId()));
        assertNotNull(found);
        assertEquals(owner.getId(), found.getFunction().getOwner().getId());
    }
}