package com.cloudshare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class StorageConfig {

    @Value("${cloudshare.s3.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${cloudshare.s3.public-endpoint:}")
    private String publicEndpoint;

    @Value("${cloudshare.s3.region:us-east-1}")
    private String region;

    @Value("${cloudshare.s3.access-key:minioadmin}")
    private String accessKey;

    @Value("${cloudshare.s3.secret-key:minioadmin}")
    private String secretKey;

    @Value("${cloudshare.s3.path-style:true}")
    private boolean pathStyle;

    @Bean
    public S3Client s3Client() {
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyle)
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyle)
                .build();

        String presignerEndpoint = (publicEndpoint != null && !publicEndpoint.isBlank())
                ? publicEndpoint.trim()
                : endpoint;

        return S3Presigner.builder()
                .endpointOverride(URI.create(presignerEndpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(serviceConfiguration)
                .build();
    }
}
