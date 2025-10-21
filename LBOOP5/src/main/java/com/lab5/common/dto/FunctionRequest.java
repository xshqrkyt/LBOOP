package com.lab5.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class FunctionRequest {
    private String name;
    private String type;
    private List<PointRequest> points;
}
