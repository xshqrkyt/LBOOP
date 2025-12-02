package com.lab7.dto;

public class OperationResponse {
    private Double[] x;
    private Double[] y;
    private Double applyResult;
    private Long functionId;
    private String message;

    public OperationResponse() {
    }

    public OperationResponse(Double[] x, Double[] y, Double applyResult, Long functionId, String message) {
        this.x = x;
        this.y = y;
        this.applyResult = applyResult;
        this.functionId = functionId;
        this.message = message;
    }

    public Double[] getX() {
        return x;
    }

    public void setX(Double[] x) {
        this.x = x;
    }

    public Double[] getY() {
        return y;
    }

    public void setY(Double[] y) {
        this.y = y;
    }

    public Double getApplyResult() {
        return applyResult;
    }

    public void setApplyResult(Double applyResult) {
        this.applyResult = applyResult;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
