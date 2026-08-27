package com.example.cognitobackend.dto;

import jakarta.validation.constraints.NotBlank;

public class TodoDtos {

    public record CreateTodoRequest(
            @NotBlank String text
    ) {}
}