package com.lab7.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "points")
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Exclude
    private Long id;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "x_value", nullable = false)
    @EqualsAndHashCode.Exclude
    private double[] xValue;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "y_value", nullable = false)
    @EqualsAndHashCode.Exclude
    private double[] yValue;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Function function;
}