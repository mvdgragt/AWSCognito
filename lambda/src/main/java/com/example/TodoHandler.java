package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TodoHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final CognitoIdentityProviderClient cognito = CognitoIdentityProviderClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String TABLE = "todos";
    private static final Map<String, String> CORS = Map.of(
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Headers", "Content-Type,Authorization",
            "Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS"
    );

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        String method = event.getRequestContext().getHttp().getMethod();

        if ("OPTIONS".equals(method)) {
            return res(200, "");
        }

        String routeKey = event.getRouteKey();
        String userId = event.getRequestContext().getAuthorizer().getJwt().getClaims().get("sub");

        try {
            return switch (routeKey) {
                case "GET /todos" -> getTodos(userId);
                case "POST /todos" -> addTodo(userId, event.getBody());
                case "DELETE /todos" -> deleteTodo(userId, event.getBody());
                case "DELETE /account" -> deleteAccount(userId, event.getHeaders());
                default -> res(405, "{\"error\":\"Method not allowed\"}");
            };
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return res(500, "{\"error\":\"Internal server error\"}");
        }
    }

    private APIGatewayV2HTTPResponse getTodos(String userId) throws Exception {
        var result = dynamoDb.query(QueryRequest.builder()
                .tableName(TABLE)
                .keyConditionExpression("userId = :uid")
                .expressionAttributeValues(Map.of(":uid", AttributeValue.fromS(userId)))
                .build());

        var items = result.items().stream().map(item -> {
            var map = new HashMap<String, Object>();
            map.put("userId", item.get("userId").s());
            map.put("todoId", item.get("todoId").s());
            map.put("text", item.get("text").s());
            map.put("done", item.get("done").bool());
            map.put("createdAt", item.get("createdAt").s());
            return map;
        }).toList();

        return res(200, mapper.writeValueAsString(items));
    }

    private APIGatewayV2HTTPResponse addTodo(String userId, String body) throws Exception {
        var data = mapper.readValue(body, Map.class);
        var todoId = UUID.randomUUID().toString();
        var createdAt = Instant.now().toString();

        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(TABLE)
                .item(Map.of(
                        "userId", AttributeValue.fromS(userId),
                        "todoId", AttributeValue.fromS(todoId),
                        "text", AttributeValue.fromS((String) data.get("text")),
                        "done", AttributeValue.fromBool(false),
                        "createdAt", AttributeValue.fromS(createdAt)
                ))
                .build());

        var todo = Map.of(
                "userId", userId,
                "todoId", todoId,
                "text", data.get("text"),
                "done", false,
                "createdAt", createdAt
        );
        return res(201, mapper.writeValueAsString(todo));
    }

    private APIGatewayV2HTTPResponse deleteTodo(String userId, String body) throws Exception {
        var data = mapper.readValue(body, Map.class);
        var todoId = (String) data.get("todoId");

        dynamoDb.deleteItem(DeleteItemRequest.builder()
                .tableName(TABLE)
                .key(Map.of(
                        "userId", AttributeValue.fromS(userId),
                        "todoId", AttributeValue.fromS(todoId)
                ))
                .build());

        return res(200, "{\"deleted\":true}");
    }

    private APIGatewayV2HTTPResponse deleteAccount(String userId, Map<String, String> headers) throws Exception {
        var result = dynamoDb.query(QueryRequest.builder()
                .tableName(TABLE)
                .keyConditionExpression("userId = :uid")
                .expressionAttributeValues(Map.of(":uid", AttributeValue.fromS(userId)))
                .build());

        for (var item : result.items()) {
            dynamoDb.deleteItem(DeleteItemRequest.builder()
                    .tableName(TABLE)
                    .key(Map.of(
                            "userId", AttributeValue.fromS(userId),
                            "todoId", item.get("todoId")
                    ))
                    .build());
        }

        String auth = headers.getOrDefault("authorization", headers.getOrDefault("Authorization", ""));
        String accessToken = auth.replace("Bearer ", "");

        try {
            cognito.deleteUser(DeleteUserRequest.builder().accessToken(accessToken).build());
        } catch (UserNotFoundException e) {
            // already deleted, ignore
        }

        return res(200, "{\"deleted\":true}");
    }

    private APIGatewayV2HTTPResponse res(int statusCode, String body) {
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(statusCode)
                .withHeaders(CORS)
                .withBody(body)
                .build();
    }
}
