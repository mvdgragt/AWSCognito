package com.example.cognitobackend.controller;

import com.example.cognitobackend.repository.TodoRepository;
import com.example.cognitobackend.service.CognitoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final TodoRepository todoRepository;
    private final CognitoService cognitoService;

    public AccountController(TodoRepository todoRepository, CognitoService cognitoService) {
        this.todoRepository = todoRepository;
        this.cognitoService = cognitoService;
    }

    @DeleteMapping
    public void deleteAccount(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String accessToken = jwt.getTokenValue();

        todoRepository.deleteAllByUserId(userId);
        cognitoService.deleteUser(accessToken);
    }
}