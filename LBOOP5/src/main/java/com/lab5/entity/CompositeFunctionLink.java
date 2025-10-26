package com.lab5.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeFunctionLink {
    private Long id;
    private Long compositeId;
    private Long functionId;
    private Integer orderIndex;
}