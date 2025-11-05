package com.lab6.entity;

import lombok.*;

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
