package com.lab6.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointRequest {
    private Double x;
    private Double y;
    private Long functionId;
}