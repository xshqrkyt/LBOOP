package com.lab7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointRequest {
    @EqualsAndHashCode.Exclude
    @JsonProperty("xValues")
    private double[] x;

    @EqualsAndHashCode.Exclude
    @JsonProperty("yValues")
    private double[] y;

    @EqualsAndHashCode.Exclude
    private Long functionId;
}