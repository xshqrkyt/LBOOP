package com.lab7.entity;

public class Points {
    private Long id;
    private Double[] xValues;
    private Double[] yValues;
    private Long functionId;

    public Points() {
    }

    public Points(Long id, Double[] xValues, Double[] yValues, Long functionId) {
        this.id = id;
        this.xValues = xValues;
        this.yValues = yValues;
        this.functionId = functionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double[] getXValues() {
        return xValues;
    }

    public void setXValues(Double[] xValues) {
        this.xValues = xValues;
    }

    public Double[] getYValues() {
        return yValues;
    }

    public void setYValues(Double[] yValues) {
        this.yValues = yValues;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }
}
