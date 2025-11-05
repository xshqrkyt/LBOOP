package com.lab6.entity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeFunctionLink {
    @EqualsAndHashCode.Exclude
    private Long id;

    @EqualsAndHashCode.Exclude
    private Long compositeId;

    @EqualsAndHashCode.Exclude
    private Long functionId;

    @EqualsAndHashCode.Exclude
    private Integer orderIndex;
}