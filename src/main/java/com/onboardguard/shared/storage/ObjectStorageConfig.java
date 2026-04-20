package com.onboardguard.shared.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class ObjectStorageConfig {

    // Map to app.storage instead of cloud.aws
    @Value("${app.storage.region:auto}")
    private String region;

    @Value("${app.storage.credentials.access-key:}")
    private String accessKey;

    @Value("${app.storage.credentials.secret-key:}")
    private String secretKey;

    @Value("${app.storage.endpoint:}")
    private String endpointOverride;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider());

        if (!endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride))
                    // CRITICAL for R2 / MinIO
                    .serviceConfiguration(s -> s.pathStyleAccessEnabled(true));
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider());

        if (!endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }

    private software.amazon.awssdk.auth.credentials.AwsCredentialsProvider credentialsProvider() {
        if (accessKey != null && !accessKey.isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));
        }
        return DefaultCredentialsProvider.create();
    }
}