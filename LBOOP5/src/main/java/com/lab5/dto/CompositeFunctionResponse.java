package com.lab5.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeFunctionResponse {
    private Long id;
    private String name;
    private Long ownerId;
}