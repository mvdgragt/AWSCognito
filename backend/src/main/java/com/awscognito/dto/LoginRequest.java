package com.awscognito.dto;

public record LoginRequest(
        String email,
        String password
) {
}