package com.lab6.entity;

import com.lab6.enums.FunctionType;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Function {
    @EqualsAndHashCode.Exclude
    private Long id;

    @EqualsAndHashCode.Exclude
    private String name;

    @EqualsAndHashCode.Exclude
    private FunctionType type;

    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @EqualsAndHashCode.Exclude
    private Long ownerId;
}