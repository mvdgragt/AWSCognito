package com.example.cognitobackend.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.UUID;

@DynamoDbBean
public class Todo {

    private String userId;
    private String todoId;
    private String text;
    private boolean done;

    public Todo() {}

    public static Todo newTodo(String userId, String text) {
        Todo todo = new Todo();
        todo.userId = userId;
        todo.todoId = UUID.randomUUID().toString();
        todo.text = text;
        todo.done = false;
        return todo;
    }

    @DynamoDbPartitionKey
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSortKey
    public String getTodoId() { return todoId; }
    public void setTodoId(String todoId) { this.todoId = todoId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}