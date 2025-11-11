package com.lab7.controllers;

import com.lab7.dto.*;
import com.lab7.dto.UserRequest;
import com.lab7.dto.UserResponse;
import com.lab7.enums.UserRole;
import com.lab7.entity.User;
import com.lab7.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/lab6-1.0-SNAPSHOT/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(Long.valueOf(user.getId()), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    private User toEntity(UserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPasswordHash());
        user.setEmail(request.getEmail());
        user.setRole(UserRole.valueOf(request.getRole()));
        return user;
    }

    @GetMapping
    public ResponseEntity<?> getUser(@RequestParam(required = false) Long id, @RequestParam(required = false) String username) {
        log.info("Получен запрос Get /spring/users.");

        if (id != null)
            return userRepository.findById(id).map(user -> ResponseEntity.ok(toResponse(user))).orElseGet(() -> ResponseEntity.notFound().build());

        else if (username != null)
            return userRepository.findByUsername(username).map(user -> ResponseEntity.ok(toResponse(user))).orElseGet(() -> ResponseEntity.notFound().build());

        else {
            // Если нет параметров, возвращаем всех пользователей
            List<UserResponse> users = userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(users);
        }
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        log.info("Получен запрос POST /spring/users с телом: {}", request);
        User entity = toEntity(request);
        User saved = userRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        log.info("Получен запрос PUT /spring/users/{} с телом: {}", id, request);
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setUsername(request.getUsername());
            existingUser.setPasswordHash(request.getPasswordHash());
            existingUser.setEmail(request.getEmail());
            existingUser.setRole(UserRole.valueOf(request.getRole()));
            userRepository.save(existingUser);
            log.info("Пользователь обновлён.");
            return ResponseEntity.ok(toResponse(existingUser));
        }).orElseGet(() -> {
            log.warn("Пользователь с id {} не найден для обновления", id);
            return ResponseEntity.notFound().build();
        });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Получен запрос DELETE /spring/users/{}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("Удален пользователь с id {}", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Пользователь с id {} не найден для удаления", id);
            return ResponseEntity.notFound().build();
        }
    }
}