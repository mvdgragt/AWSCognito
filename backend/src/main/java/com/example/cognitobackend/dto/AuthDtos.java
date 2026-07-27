package com.example.cognitobackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public class AuthDtos {

    public record SignUpRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record ConfirmSignUpRequest(
            @Email @NotBlank String email,
            @NotBlank String code
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record LoginResponse(
            String accessToken,
            String idToken,
            String refreshToken,
            long expiresIn
    ) {}

    public record MeResponse(
            String sub,
            String email,
            Instant expiresAt,
            List<String> scopes
    ) {}
}