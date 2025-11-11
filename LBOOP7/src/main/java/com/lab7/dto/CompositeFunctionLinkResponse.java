package com.lab7.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeFunctionLinkResponse {
    private Long id;
    private Long compositeId;
    private Long functionId;
    private Integer orderIndex;
}