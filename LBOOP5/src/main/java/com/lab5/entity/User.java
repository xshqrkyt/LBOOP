package com.lab5.entity;

import com.lab5.enums.UserRole;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;

    public User(Long id, String username, String passwordHash, String email, UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }
}