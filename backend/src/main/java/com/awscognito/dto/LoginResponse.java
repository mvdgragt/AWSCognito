package com.awscognito.dto;

public record LoginResponse(
        String accessToken,
        String idToken,
        String refreshToken,
        String email

) {
}