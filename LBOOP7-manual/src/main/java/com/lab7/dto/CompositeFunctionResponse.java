package com.lab7.dto;

import java.time.LocalDateTime;

public class CompositeFunctionResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Long ownerId;

    public CompositeFunctionResponse() {
    }

    public CompositeFunctionResponse(Long id, String name, LocalDateTime createdAt, Long ownerId) {
        this.id = id;
        this.name = name;
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
