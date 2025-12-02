package com.lab7.dto;

public class FunctionRequest {
    private String name;
    private String type;
    private Long ownerId;

    public FunctionRequest() {
    }

    public FunctionRequest(String name, String type, Long ownerId) {
        this.name = name;
        this.type = type;
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
