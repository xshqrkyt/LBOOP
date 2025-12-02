package com.lab7.dto;

public class OperationRequest {
    private String operation;
    private Long firstFunctionId;
    private Long secondFunctionId;
    private Double applyX;
    private Integer threads;
    private String factory;
    private String resultName;

    public OperationRequest() {
    }

    public OperationRequest(String operation, Long firstFunctionId, Long secondFunctionId, Double applyX,
                             Integer threads, String factory, String resultName) {
        this.operation = operation;
        this.firstFunctionId = firstFunctionId;
        this.secondFunctionId = secondFunctionId;
        this.applyX = applyX;
        this.threads = threads;
        this.factory = factory;
        this.resultName = resultName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Long getFirstFunctionId() {
        return firstFunctionId;
    }

    public void setFirstFunctionId(Long firstFunctionId) {
        this.firstFunctionId = firstFunctionId;
    }

    public Long getSecondFunctionId() {
        return secondFunctionId;
    }

    public void setSecondFunctionId(Long secondFunctionId) {
        this.secondFunctionId = secondFunctionId;
    }

    public Double getApplyX() {
        return applyX;
    }

    public void setApplyX(Double applyX) {
        this.applyX = applyX;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }
}
