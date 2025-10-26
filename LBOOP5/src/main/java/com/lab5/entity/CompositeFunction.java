package com.lab5.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeFunction {
    private Long id;
    private String name;
    private Long ownerId;
    private LocalDateTime createdAt;
}