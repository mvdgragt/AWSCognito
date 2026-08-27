package com.example.cognitobackend.controller;

import com.example.cognitobackend.dto.TodoDtos.CreateTodoRequest;
import com.example.cognitobackend.model.Todo;
import com.example.cognitobackend.repository.TodoRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoRepository todoRepository;

    public TodoController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping
    public List<Todo> getTodos(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return todoRepository.findAllByUserId(userId);
    }

    @PostMapping
    public Todo createTodo(@AuthenticationPrincipal Jwt jwt,
                           @Valid @RequestBody CreateTodoRequest request) {
        String userId = jwt.getSubject();
        Todo todo = Todo.newTodo(userId, request.text());
        return todoRepository.save(todo);
    }

    @DeleteMapping("/{todoId}")
    public void deleteTodo(@AuthenticationPrincipal Jwt jwt,
                           @PathVariable String todoId) {
        String userId = jwt.getSubject();
        todoRepository.delete(userId, todoId);
    }
}