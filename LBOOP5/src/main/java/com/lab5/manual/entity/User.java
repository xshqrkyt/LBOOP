package com.lab5.manual.entity;

import com.lab5.common.enums.UserRole;
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
}
