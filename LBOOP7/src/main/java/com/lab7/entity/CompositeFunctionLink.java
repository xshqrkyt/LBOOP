package com.lab7.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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