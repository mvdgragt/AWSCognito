package com.awscognito.config;

@Configuration
public class CognitoConfig {

    @Value("${aws.cognito.access-key}")
    private String accessKey;

    @Value("${aws.cognito.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Bean
    public CognitoIdentityProviderClient cognitoClient() {
        return CognitoIdentityProviderClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
                ))
            .build();
    }
}