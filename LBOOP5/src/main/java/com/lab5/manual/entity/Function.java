package com.lab5.manual.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Function {
    private Long id;
    private String name;
    private String type;
    private LocalDateTime createdAt;
    private Long ownerId;
}
