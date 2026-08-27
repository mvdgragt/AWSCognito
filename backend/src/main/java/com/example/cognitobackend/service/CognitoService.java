package com.example.cognitobackend.service;

import com.example.cognitobackend.dto.AuthDtos.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

@Service
public class CognitoService {

    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.client-id}")
    private String clientId;

    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public void signUp(String email, String password) {
        SignUpRequest request = SignUpRequest.builder()
                .clientId(clientId)
                .username(email)
                .password(password)
                .userAttributes(AttributeType.builder().name("email").value(email).build())
                .build();
        cognitoClient.signUp(request);
    }

    public void confirmSignUp(String email, String code) {
        ConfirmSignUpRequest request = ConfirmSignUpRequest.builder()
                .clientId(clientId)
                .username(email)
                .confirmationCode(code)
                .build();
        cognitoClient.confirmSignUp(request);
    }

    public LoginResponse login(String email, String password) {
        InitiateAuthRequest request = InitiateAuthRequest.builder()
                .clientId(clientId)
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(Map.of("USERNAME", email, "PASSWORD", password))
                .build();

        InitiateAuthResponse response = cognitoClient.initiateAuth(request);
        AuthenticationResultType result = response.authenticationResult();

        return new LoginResponse(
                result.accessToken(),
                result.idToken(),
                result.refreshToken(),
                result.expiresIn()
        );
    }

    public void deleteUser(String accessToken) {
        DeleteUserRequest request = DeleteUserRequest.builder()
                .accessToken(accessToken)
                .build();
        cognitoClient.deleteUser(request);
    }
}