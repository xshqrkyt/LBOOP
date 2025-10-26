package com.lab5.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeFunctionLinkRequest {
    private Long compositeId;
    private Long functionId;
    private Integer orderIndex;
}