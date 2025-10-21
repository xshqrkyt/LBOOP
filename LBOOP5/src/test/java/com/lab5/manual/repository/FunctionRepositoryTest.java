// src/test/java/com/lab5/manual/repository/FunctionRepositoryTest.java
package com.lab5.manual.repository;

import com.lab5.manual.entity.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class FunctionRepositoryTest {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        // Create test user
        com.lab5.manual.entity.User user = new com.lab5.manual.entity.User();
        user.setUsername("testuser");
        user.setPasswordHash("password123");
        user.setEmail("test@example.com");
        user.setRole(com.lab5.common.enums.UserRole.USER);
        testUserId = userRepository.create(user);
    }

    @Test
    void testCreateFunction() {
        Function function = new Function();
        function.setName("Test Function");
        function.setType("TABULATED");
        function.setOwnerId(testUserId);

        Long functionId = functionRepository.create(function);
        assertNotNull(functionId);
        assertTrue(functionId > 0);
    }

    @Test
    void testFindById() {
        Function function = new Function();
        function.setName("Test Function");
        function.setType("TABULATED");
        function.setOwnerId(testUserId);
        Long functionId = functionRepository.create(function);

        Function found = functionRepository.findById(functionId).orElse(null);
        assertNotNull(found);
        assertEquals("Test Function", found.getName());
        assertEquals("TABULATED", found.getType());
        assertEquals(testUserId, found.getOwnerId());
    }

    @Test
    void testFindByOwnerId() {
        // Create multiple functions for the same user
        Function function1 = new Function();
        function1.setName("Function 1");
        function1.setType("TABULATED");
        function1.setOwnerId(testUserId);
        functionRepository.create(function1);

        Function function2 = new Function();
        function2.setName("Function 2");
        function2.setType("SQR");
        function2.setOwnerId(testUserId);
        functionRepository.create(function2);

        List<Function> functions = functionRepository.findByOwnerId(testUserId);
        assertEquals(2, functions.size());
        assertTrue(functions.stream().anyMatch(f -> f.getName().equals("Function 1")));
        assertTrue(functions.stream().anyMatch(f -> f.getName().equals("Function 2")));
    }

    @Test
    void testFindByNameContaining() {
        Function function1 = new Function();
        function1.setName("Linear Function Test");
        function1.setType("TABULATED");
        function1.setOwnerId(testUserId);
        functionRepository.create(function1);

        Function function2 = new Function();
        function2.setName("Quadratic Function");
        function2.setType("SQR");
        function2.setOwnerId(testUserId);
        functionRepository.create(function2);

        List<Function> functions = functionRepository.findByNameContaining("Function");
        assertEquals(2, functions.size());

        List<Function> linearFunctions = functionRepository.findByNameContaining("Linear");
        assertEquals(1, linearFunctions.size());
        assertEquals("Linear Function Test", linearFunctions.get(0).getName());
    }

    @Test
    void testDeleteFunction() {
        Function function = new Function();
        function.setName("To Delete");
        function.setType("TABULATED");
        function.setOwnerId(testUserId);
        Long functionId = functionRepository.create(function);

        // Verify function exists
        assertTrue(functionRepository.findById(functionId).isPresent());

        // Delete function
        functionRepository.delete(functionId);

        // Verify function is deleted
        assertFalse(functionRepository.findById(functionId).isPresent());
    }
}