package com.lab7.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionResponse {
    private Long id;
    private String name;
    private String type;
    private Long ownerId;
}