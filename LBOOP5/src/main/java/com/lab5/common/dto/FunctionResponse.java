package com.lab5.common.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FunctionResponse {
    private Long id;
    private String name;
    private String type;
    private LocalDateTime createdAt;
    private Long ownerId;
    private List<PointResponse> points;
}
