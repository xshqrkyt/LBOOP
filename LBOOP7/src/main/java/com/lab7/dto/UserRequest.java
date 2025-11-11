package com.lab7.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @EqualsAndHashCode.Exclude
    private String username;

    @EqualsAndHashCode.Exclude
    private String passwordHash;

    @EqualsAndHashCode.Exclude
    private String email;

    @EqualsAndHashCode.Exclude
    private String role;
}