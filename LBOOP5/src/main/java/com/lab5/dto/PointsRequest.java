package com.lab5.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsRequest {
    private Double[] xValues;
    private Double[] yValues;
    private Long functionId;
}