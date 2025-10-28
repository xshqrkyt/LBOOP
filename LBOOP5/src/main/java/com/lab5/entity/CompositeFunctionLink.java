package com.lab5.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "composite_function_link")
public class CompositeFunctionLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composite_id", nullable = false)
    private CompositeFunction compositeFunction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private Function function;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}