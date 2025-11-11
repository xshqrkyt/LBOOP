package com.lab7.entity;

import com.lab7.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Exclude
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    @EqualsAndHashCode.Exclude
    private String username;

    @Column(name = "password_hash", nullable = false)
    @EqualsAndHashCode.Exclude
    private String passwordHash;

    @Column(name = "email")
    @EqualsAndHashCode.Exclude
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @EqualsAndHashCode.Exclude
    private UserRole role;

    @CreationTimestamp
    @Column(name = "created_at")
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Function> functions = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompositeFunction> compositeFunctions = new ArrayList<>();
}