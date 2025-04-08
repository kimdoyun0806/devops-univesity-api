package com.beyond.university.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class LoginRequestDto {
    @NotBlank
    private final String username;

    @NotBlank
    private final String password;
}
