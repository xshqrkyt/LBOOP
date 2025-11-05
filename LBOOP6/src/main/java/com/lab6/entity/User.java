package com.lab6.entity;

import com.lab6.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @EqualsAndHashCode.Exclude
    private Long id;

    @EqualsAndHashCode.Exclude
    private String username;

    @EqualsAndHashCode.Exclude
    private String passwordHash;

    @EqualsAndHashCode.Exclude
    private String email;

    @EqualsAndHashCode.Exclude
    private UserRole role;

    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;
}