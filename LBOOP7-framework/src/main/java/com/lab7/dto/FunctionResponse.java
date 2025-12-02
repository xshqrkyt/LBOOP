package com.lab7.dto;

import com.lab7.enums.FunctionType;

import java.time.LocalDateTime;

public class FunctionResponse {
    private Long id;
    private String name;
    private FunctionType type;
    private LocalDateTime createdAt;
    private Long ownerId;

    public FunctionResponse() {
    }

    public FunctionResponse(Long id, String name, FunctionType type, LocalDateTime createdAt, Long ownerId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FunctionType getType() {
        return type;
    }

    public void setType(FunctionType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
