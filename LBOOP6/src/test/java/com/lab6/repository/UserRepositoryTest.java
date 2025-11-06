package com.lab6.repository;

import com.lab6.entity.*;
import com.lab6.enums.UserRole;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.domain.Sort;
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
    public void testFindByEmailAndRole() {
        List<User> users = userRepository.findByEmailAndRole("testuser@example.com", UserRole.USER);
        assertThat(users).isNotEmpty();
        assertThat(users).allMatch(u -> u.getEmail().equals("testuser@example.com") && u.getRole() == UserRole.USER);
    }

    @Test
    public void testFindByUsernameWithSort() {
        User user1 = new User();
        user1.setUsername("testuser1"); // уникальное имя
        user1.setPasswordHash("hash1");
        user1.setEmail("testuser1@example.com");
        user1.setRole(UserRole.USER);
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("testuser2"); // другое уникальное имя
        user2.setPasswordHash("hash2");
        user2.setEmail("testuser2@example.com");
        user2.setRole(UserRole.ADMIN);
        userRepository.save(user2);

        // Теперь вызываем поиск по username, например, по "testuser1"
        List<User> users = userRepository.findByUsername("testuser1", Sort.by(Sort.Direction.ASC, "role"));
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("testuser1");
    }
}