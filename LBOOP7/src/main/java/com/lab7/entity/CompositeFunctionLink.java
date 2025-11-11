package com.lab7.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "composite_function_link")
public class CompositeFunctionLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Exclude
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composite_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private CompositeFunction compositeFunction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Function function;

    @Column(name = "order_index", nullable = false)
    @EqualsAndHashCode.Exclude
    private Integer orderIndex;
}