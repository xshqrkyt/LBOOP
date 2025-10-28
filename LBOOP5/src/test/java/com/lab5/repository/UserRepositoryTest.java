package com.lab5.repository;

import com.lab5.common.enums.*;
import com.lab5.entity.*;
import com.lab5.repository.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hashpassword");
        testUser.setEmail("testuser@example.com");
        testUser.setRole(UserRole.USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    public void testFindByUsername() {
        Optional<User> found = userRepository.findByUsername("testuser");
        assertTrue(found.isPresent());
        assertEquals(testUser.getId(), found.get().getId());
    }

    @Test
    public void testFindByEmailContainingIgnoreCase() {
        List<User> users = userRepository.findByEmailContainingIgnoreCase("EXAMPLE");
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase("testuser@example.com")));
    }

    @Test
    public void testExistsByUsername() {
        boolean exists = userRepository.existsByUsername("testuser");
        assertTrue(exists);

        boolean notExists = userRepository.existsByUsername("nonexistent");
        assertFalse(notExists);
    }

    @Test
    public void testFindByIdWithFunctions() {
        Optional<User> found = userRepository.findByIdWithFunctions(testUser.getId());
        assertTrue(found.isPresent());
        assertEquals(testUser.getId(), found.get().getId());
        // Initially functions list empty
        assertNotNull(found.get().getFunctions());
        assertEquals(0, found.get().getFunctions().size());
    }
}