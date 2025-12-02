package com.lab7.dto;

public class CompositeFunctionLinkResponse {
    private Long id;
    private Long compositeId;
    private Long functionId;
    private Integer orderIndex;

    public CompositeFunctionLinkResponse() {
    }

    public CompositeFunctionLinkResponse(Long id, Long compositeId, Long functionId, Integer orderIndex) {
        this.id = id;
        this.compositeId = compositeId;
        this.functionId = functionId;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
