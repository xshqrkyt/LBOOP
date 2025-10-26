package com.lab5.entity;

import com.lab5.enums.FunctionType;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Function {
    private Long id;
    private String name;
    private FunctionType type;
    private LocalDateTime createdAt;
    private Long ownerId;

    public Function(Long id, String name, FunctionType type, LocalDateTime createdAt, Long ownerId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
    }
}