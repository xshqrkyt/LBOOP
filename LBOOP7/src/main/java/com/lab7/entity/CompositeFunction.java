package com.lab7.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@Table(name = "composite_function")
public class CompositeFunction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Exclude
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @EqualsAndHashCode.Exclude
    private String name;

    @CreationTimestamp
    @Column(name = "created_at")
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private User owner;

    @OneToMany(mappedBy = "compositeFunction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompositeFunctionLink> compositeLinks = new ArrayList<>();
}