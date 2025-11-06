package com.lab6.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointResponse {
    private Long id;
    private Double x;
    private Double y;
    private Long functionId;
}