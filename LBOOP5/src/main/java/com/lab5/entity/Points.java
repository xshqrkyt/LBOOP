package com.lab5.entity;

import lombok.Data;

@Data
public class Points {
    private Long id;
    private Double[] xValues;
    private Double[] yValues;
    private Long functionId;

    public Points(Long id, Double[] xValues, Double[] yValues, Long functionId) {
        this.id = id;
        this.xValues = xValues;
        this.yValues = yValues;
        this.functionId = functionId;
    }
}
