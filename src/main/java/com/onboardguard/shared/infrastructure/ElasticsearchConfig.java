package com.onboardguard.shared.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

/**
 * Elasticsearch infrastructure and boundary configuration for OnboardGuard.
 *
 * @EnableElasticsearchRepositories — STRICTLY isolates ES repositories to the
 * watchlist.elasticsearch package.
 * CRITICAL: Prevents Spring from trying to map our
 * PostgreSQL JPA repositories as ES documents.
 *
 * Who uses Elasticsearch in this project (CQRS Pattern):
 *
 * WatchlistSyncListener     → (WRITE) Listens to WatchlistEntryUpdatedEvent and pushes
 * PostgreSQL data into the ES cluster to keep them in sync.
 *
 * AdvancedScreeningStrategy → (READ) Queries ES during Candidate Onboarding to calculate
 * risk scores using fuzzy names, phonetic matches, and aliases.
 *
 * Connection is tuned for Remote/Cloud Enterprise environments:
 * - URL sanitization prevents underlying Java socket errors.
 * - Timeouts ensure the Screening Engine fails fast rather than hanging indefinitely.
 * - Dynamically adapts to internal VPC clusters (No Auth) vs Secure Cloud clusters (Auth).
 */
@Slf4j
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.onboardguard.watchlist.elasticsearch")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    // URI is strictly required. App will fail to boot if missing.
    @Value("${spring.elasticsearch.uris}")
    private String esUrl;

    // The colon ':' makes these optional. If missing from YAML, they become empty strings.
    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Value("${spring.elasticsearch.connection-timeout:5000}")
    private long connectionTimeout;

    @Value("${spring.elasticsearch.socket-timeout:30000}")
    private long socketTimeout;

    @Override
    public ClientConfiguration clientConfiguration() {

        // 1. Detect protocols and auth states
        boolean isSecure = esUrl.startsWith("https://");
        boolean hasAuth = (username != null && !username.isBlank());

        // 2. The builder strictly requires "host:port", so we strip the protocol
        String cleanUrl = esUrl.replace("http://", "").replace("https://", "");

        log.info("Connecting to Remote Elasticsearch Cluster at: {} (Secure SSL: {}, Auth Enabled: {})",
                cleanUrl, isSecure, hasAuth);

        // 3. Build the configuration based on what the Senior provided to avoid Builder compilation errors

        if (isSecure && hasAuth) {
            // HTTPS + Credentials
            return ClientConfiguration.builder()
                    .connectedTo(cleanUrl)
                    .usingSsl()
                    .withConnectTimeout(Duration.ofMillis(connectionTimeout))
                    .withSocketTimeout(Duration.ofMillis(socketTimeout))
                    .withBasicAuth(username, password)
                    .build();

        } else if (isSecure && !hasAuth) {
            // HTTPS + No Credentials (Likely internal network with SSL)
            return ClientConfiguration.builder()
                    .connectedTo(cleanUrl)
                    .usingSsl()
                    .withConnectTimeout(Duration.ofMillis(connectionTimeout))
                    .withSocketTimeout(Duration.ofMillis(socketTimeout))
                    .build();

        } else if (!isSecure && hasAuth) {
            // HTTP + Credentials
            return ClientConfiguration.builder()
                    .connectedTo(cleanUrl)
                    .withConnectTimeout(Duration.ofMillis(connectionTimeout))
                    .withSocketTimeout(Duration.ofMillis(socketTimeout))
                    .withBasicAuth(username, password)
                    .build();

        } else {
            // HTTP + No Credentials (Standard internal VPC or local Docker)
            return ClientConfiguration.builder()
                    .connectedTo(cleanUrl)
                    .withConnectTimeout(Duration.ofMillis(connectionTimeout))
                    .withSocketTimeout(Duration.ofMillis(socketTimeout))
                    .build();
        }
    }
}