package com.lab7.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "functions")
public class Function {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Exclude
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @EqualsAndHashCode.Exclude
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    @EqualsAndHashCode.Exclude
    private String type;

    @CreationTimestamp
    @Column(name = "created_at")
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private User owner;

    @OneToOne(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private Point points;

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompositeFunctionLink> compositeLinks = new ArrayList<>();
}