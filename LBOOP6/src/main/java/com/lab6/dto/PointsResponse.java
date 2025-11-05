package com.lab6.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsResponse {
    private Long id;

    @JsonProperty("xValues")
    private Double[] xValues;

    @JsonProperty("yValues")
    private Double[] yValues;
    private Long functionId;
}