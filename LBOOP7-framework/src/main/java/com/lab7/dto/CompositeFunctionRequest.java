package com.lab7.dto;

public class CompositeFunctionRequest {
    private String name;
    private Long ownerId;

    public CompositeFunctionRequest() {
    }

    public CompositeFunctionRequest(String name, Long ownerId) {
        this.name = name;
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
