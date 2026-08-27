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
        return response(HttpStatus.CONFLICT, "Det finns redan ett konto med den e-postadressen");
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        return response(HttpStatus.BAD_REQUEST, "Lösenordet uppfyller inte kraven");
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParameter(InvalidParameterException ex) {
        return response(HttpStatus.BAD_REQUEST, "Ogiltiga uppgifter");
    }

    @ExceptionHandler({CodeMismatchException.class, ExpiredCodeException.class})
    public ResponseEntity<Map<String, String>> handleBadCode(RuntimeException ex) {
        return response(HttpStatus.BAD_REQUEST, "Fel eller utgången bekräftelsekod");
    }

    @ExceptionHandler(UserNotConfirmedException.class)
    public ResponseEntity<Map<String, String>> handleUserNotConfirmed(UserNotConfirmedException ex) {
        return response(HttpStatus.UNAUTHORIZED, "Kontot är inte bekräftat ännu");
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<Map<String, String>> handleNotAuthorized(NotAuthorizedException ex) {
        return response(HttpStatus.UNAUTHORIZED, "Fel e-postadress eller lösenord");
    }

    @ExceptionHandler(CognitoIdentityProviderException.class)
    public ResponseEntity<Map<String, String>> handleCognitoFallback(CognitoIdentityProviderException ex) {
        return response(HttpStatus.BAD_GATEWAY, "Ett fel uppstod, försök igen senare");
    }

    private ResponseEntity<Map<String, String>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "error", status.getReasonPhrase(),
                "message", message
        ));
    }
}