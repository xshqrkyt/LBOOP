// src/test/java/com/lab5/manual/repository/UserRepositoryTest.java
package com.lab5.manual.repository;

import com.lab5.common.enums.UserRole;
import com.lab5.manual.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateUser() {
        User user = new User();
        user.setUsername("johndoe");
        user.setPasswordHash("hashedpassword");
        user.setEmail("john@example.com");
        user.setRole(UserRole.USER);

        Long userId = userRepository.create(user);
        assertNotNull(userId);
        assertTrue(userId > 0);
    }

    @Test
    void testFindById() {
        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash("password123");
        user.setEmail("alice@example.com");
        user.setRole(UserRole.USER);
        Long userId = userRepository.create(user);

        Optional<User> found = userRepository.findById(userId);
        assertTrue(found.isPresent());
        assertEquals("alice", found.get().getUsername());
        assertEquals("alice@example.com", found.get().getEmail());
        assertEquals(UserRole.USER, found.get().getRole());
    }

    @Test
    void testFindByUsername() {
        User user = new User();
        user.setUsername("bob");
        user.setPasswordHash("password123");
        user.setEmail("bob@example.com");
        user.setRole(UserRole.ADMIN);
        userRepository.create(user);

        Optional<User> found = userRepository.findByUsername("bob");
        assertTrue(found.isPresent());
        assertEquals("bob@example.com", found.get().getEmail());
        assertEquals(UserRole.ADMIN, found.get().getRole());
    }

    @Test
    void testFindAllUsers() {
        // Create multiple users
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPasswordHash("pass1");
        user1.setEmail("user1@example.com");
        user1.setRole(UserRole.USER);
        userRepository.create(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPasswordHash("pass2");
        user2.setEmail("user2@example.com");
        user2.setRole(UserRole.ADMIN);
        userRepository.create(user2);

        List<User> users = userRepository.findAll();
        assertTrue(users.size() >= 2);
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("user1")));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("user2")));
    }

    @Test
    void testUserNotFound() {
        Optional<User> found = userRepository.findById(999999L);
        assertFalse(found.isPresent());
    }
}