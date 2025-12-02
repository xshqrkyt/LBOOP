package com.lab7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PointsRequest {
    @JsonProperty("xValues")
    private Double[] xValues;
    @JsonProperty("yValues")
    private Double[] yValues;
    private Long functionId;

    public PointsRequest() {
    }

    public PointsRequest(Double[] xValues, Double[] yValues, Long functionId) {
        this.xValues = xValues;
        this.yValues = yValues;
        this.functionId = functionId;
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
