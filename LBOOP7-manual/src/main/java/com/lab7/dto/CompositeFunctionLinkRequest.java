package com.lab7.dto;

public class CompositeFunctionLinkRequest {
    private Long compositeId;
    private Long functionId;
    private Integer orderIndex;

    public CompositeFunctionLinkRequest() {
    }

    public CompositeFunctionLinkRequest(Long compositeId, Long functionId, Integer orderIndex) {
        this.compositeId = compositeId;
        this.functionId = functionId;
        this.orderIndex = orderIndex;
    }

    public Long getCompositeId() {
        return compositeId;
    }

    public void setCompositeId(Long compositeId) {
        this.compositeId = compositeId;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
