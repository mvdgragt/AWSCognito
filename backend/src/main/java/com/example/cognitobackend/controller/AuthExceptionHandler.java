package com.example.cognitobackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeMismatchException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ExpiredCodeException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotConfirmedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<Map<String, String>> handleUsernameExists(UsernameExistsException ex) {
        return response(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler({InvalidPasswordException.class, InvalidParameterException.class, CodeMismatchException.class, ExpiredCodeException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        return response(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(UserNotConfirmedException.class)
    public ResponseEntity<Map<String, String>> handleUserNotConfirmed(UserNotConfirmedException ex) {
        return response(HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<Map<String, String>> handleNotAuthorized(NotAuthorizedException ex) {
        return response(HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(CognitoIdentityProviderException.class)
    public ResponseEntity<Map<String, String>> handleCognitoFallback(CognitoIdentityProviderException ex) {
        return response(HttpStatus.BAD_GATEWAY, ex);
    }

    private ResponseEntity<Map<String, String>> response(HttpStatus status, RuntimeException ex) {
        return ResponseEntity.status(status).body(Map.of(
                "error", status.getReasonPhrase(),
                "message", ex.getMessage()
        ));
    }
}

