package com.example.cognitobackend.controller;

import com.example.cognitobackend.dto.AuthDtos.*;
import com.example.cognitobackend.service.CognitoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final CognitoService cognitoService;

    public AuthController(CognitoService cognitoService) {
        this.cognitoService = cognitoService;
    }

    @PostMapping("/signup")
    public void signUp(@Valid @RequestBody SignUpRequest request) {
        cognitoService.signUp(request.email(), request.password());
    }

    @PostMapping("/confirm")
    public void confirm(@Valid @RequestBody ConfirmSignUpRequest request) {
        cognitoService.confirmSignUp(request.email(), request.code());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return cognitoService.login(request.email(), request.password());
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        String scopeClaim = jwt.getClaimAsString("scope");
        List<String> scopes = (scopeClaim == null || scopeClaim.isBlank())
                ? List.of()
                : Arrays.asList(scopeClaim.split("\\s+"));

        return new MeResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getExpiresAt(),
                scopes
        );
    }
}