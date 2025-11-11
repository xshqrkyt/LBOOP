package com.lab7.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Points {
    @EqualsAndHashCode.Exclude
    private Long id;

    @EqualsAndHashCode.Exclude
    private Double[] xValues;

    @EqualsAndHashCode.Exclude
    private Double[] yValues;

    @EqualsAndHashCode.Exclude
    private Long functionId;
}
