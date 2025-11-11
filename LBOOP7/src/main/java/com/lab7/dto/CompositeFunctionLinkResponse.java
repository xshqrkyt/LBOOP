package com.lab7.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeFunctionLinkResponse {
    @EqualsAndHashCode.Exclude
    private Long id;

    @EqualsAndHashCode.Exclude
    private Long compositeId;

    @EqualsAndHashCode.Exclude
    private Long functionId;

    @EqualsAndHashCode.Exclude
    private Integer orderIndex;
}