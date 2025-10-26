package com.lab5.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsResponse {
    private Long id;
    private Double[] xValues;
    private Double[] yValues;
    private Long functionId;
}