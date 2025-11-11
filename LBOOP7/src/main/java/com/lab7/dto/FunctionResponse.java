package com.lab7.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionResponse {
    @EqualsAndHashCode.Exclude
    private Long id;

    @EqualsAndHashCode.Exclude
    private String name;

    @EqualsAndHashCode.Exclude
    private String type;

    @EqualsAndHashCode.Exclude
    private Long ownerId;
}