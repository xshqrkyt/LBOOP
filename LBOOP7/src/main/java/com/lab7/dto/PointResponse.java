package com.lab7.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointResponse {
    @EqualsAndHashCode.Exclude
    private Long id;

    @EqualsAndHashCode.Exclude
    private double[] x;

    @EqualsAndHashCode.Exclude
    private double[] y;

    @EqualsAndHashCode.Exclude
    private Long functionId;
}