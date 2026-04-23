package com.onboardguard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.onboardguard.watchlist.elasticsearch.WatchlistSearchRepository;

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

    // Provide a Mockito mock for ElasticsearchOperations so beans depending on it can initialize during tests
    @MockBean
    ElasticsearchOperations elasticsearchOperations;

    // Provide a Mockito mock for the Elasticsearch repository so services depending on it can initialize
    @MockBean
    WatchlistSearchRepository watchlistSearchRepository;

    @Test
    void contextLoads() {
    }

}
