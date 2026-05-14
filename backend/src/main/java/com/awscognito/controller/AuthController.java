package com.awscognito.controller;

import com.example.myapp.dto.*;
import com.example.myapp.service.CognitoService;
import com.example.myapp.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CognitoService cognitoService;
    private final ProfileService profileService;

    public AuthController(CognitoService cognitoService, ProfileService profileService) {
        this.cognitoService = cognitoService;
        this.profileService = profileService;
    }

    // POST /api/auth/register — public, no JWT required (configured in SecurityConfig)
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {

        cognitoService.register(request.email(), request.password());
        return ResponseEntity.ok("Registration successful. Check your email to verify.");
    }

    // POST /api/auth/login — public, no JWT required
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        AuthenticationResultType result = cognitoService.login(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(
                result.accessToken(),
                result.idToken(),
                result.refreshToken(),
                request.email()
        ));
    }

    // DELETE /api/auth/account — protected, valid JWT required (VG)

    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");


        profileService.deleteAllUserData(jwt);
        cognitoService.deleteUser(email);

        return ResponseEntity.ok("Account and all data deleted.");
    }
}