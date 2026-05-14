package com.awscognito.service;

@Service
public class CognitoService {
    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.cognito.client-id}")
    private String clientId;

    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public void register(String email, String password) {
        try {
            cognitoClient.signUp(SignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .password(password)
                    .userAttributes(AttributeType.builder()
                            .name("email")
                            .value(email)
                            .build())
                    .build());
        } catch (UsernameExistsException e) {
            throw new RuntimeException("Email already registered");
        } catch (InvalidPasswordException e) {
            throw new RuntimeException("Password does not meet requirements");
        }
    }

    public AuthenticationResultType login(String email, String password) {
        try {
            InitiateAuthResponse response = cognitoClient.initiateAuth(
                    InitiateAuthRequest.builder()
                            .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                            .clientId(clientId)
                            .authParameters(Map.of(
                                    "USERNAME", email,
                                    "PASSWORD", password))
                            .build());
            return response.authenticationResult();
        } catch (NotAuthorizedException e) {
            throw new RuntimeException("Invalid email or password");
        } catch (UserNotConfirmedException e) {
            throw new RuntimeException("Please verify your email first");
        }

        public void deleteUser(String email) {
            cognitoClient.adminDeleteUser(AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .build());
        }
    }


}
