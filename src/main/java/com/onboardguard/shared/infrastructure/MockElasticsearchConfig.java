//package com.onboardguard.shared.infrastructure;
//
//import com.onboardguard.watchlist.elasticsearch.WatchlistSearchRepository;
//import org.mockito.Mockito;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//
//@Configuration
//public class MockElasticsearchConfig {
//
//    // Provides a dummy ElasticsearchOperations bean to prevent startup crashes
//    @Bean
//    public ElasticsearchOperations elasticsearchOperations() {
//        return Mockito.mock(ElasticsearchOperations.class);
//    }
//
//    // Provides a dummy Repository bean
//    @Bean
//    public WatchlistSearchRepository watchlistSearchRepository() {
//        return Mockito.mock(WatchlistSearchRepository.class);
//    }
//}