package com.example.cognitobackend.repository;

import com.example.cognitobackend.model.Todo;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TodoRepository {

    private final DynamoDbTable<Todo> todoTable;

    public TodoRepository(DynamoDbTable<Todo> todoTable) {
        this.todoTable = todoTable;
    }

    public List<Todo> findAllByUserId(String userId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(userId).build());

        return todoTable.query(queryConditional)
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    public Todo save(Todo todo) {
        todoTable.putItem(todo);
        return todo;
    }

    public void delete(String userId, String todoId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(todoId)
                .build();
        todoTable.deleteItem(key);
    }

    public void deleteAllByUserId(String userId) {
        findAllByUserId(userId)
                .forEach(todo -> delete(userId, todo.getTodoId()));
    }

}