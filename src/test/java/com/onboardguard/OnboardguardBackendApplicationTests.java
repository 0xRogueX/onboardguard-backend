package com.onboardguard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.data.elasticsearch.repositories.enabled=false",
                "spring.elasticsearch.uris=",
                // exclude auto-config by class name to avoid direct imports that may not be resolvable
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration"
        }
)
@ActiveProfiles("test")
class OnboardguardBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
