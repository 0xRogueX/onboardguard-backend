package com.onboardguard.shared.config.service;

import com.onboardguard.shared.common.exception.ResourceNotFoundException;
import com.onboardguard.shared.config.entity.SystemConfig;
import com.onboardguard.shared.config.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository configRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final String CACHE_PREFIX = "config:";
    private final Duration CACHE_TTL = Duration.ofSeconds(60);

    // TYPE-SAFE GETTERS WITH DEFAULT VALUES
    public String getString(String key, String defaultValue) {
        return getCached(key, defaultValue);
    }

    public Integer getInt(String key, Integer defaultValue) {
        try {
            return Integer.parseInt(getCached(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            log.error("Config key '{}' is not a valid Integer. Using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        try {
            return new BigDecimal(getCached(key, defaultValue.toString()));
        } catch (Exception e) {
            log.error("Config key '{}' is not a valid BigDecimal. Using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    public Double getDouble(String key, Double defaultValue) {
        try {
            return Double.parseDouble(getCached(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            log.error("Config key '{}' is not a valid Double. Using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return Boolean.parseBoolean(getCached(key, String.valueOf(defaultValue)));
    }

    // WRITE / UPDATE OPERATIONS
    @Transactional
    public void update(String key, String newValue, Long updatedByUserId) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found: " + key));

        log.info("SystemConfig update: key='{}', updatedBy=userId:{}", key, updatedByUserId);

        config.setConfigValue(newValue);
        configRepository.save(config);

        evictCache(key); // Force Redis refresh on next read
    }

    @Transactional(readOnly = true)
    public List<SystemConfig> getAllNonSensitive() {
        return configRepository.findByIsSensitiveFalse();
    }

    // CORE RESILIENT CACHE ENGINE
    private String getCached(String key, String defaultValue) {
        String cachedKey = CACHE_PREFIX + key;

        // 1. Try to read from Redis (Gracefully handle Redis downtime)
        try {
            String cached = redisTemplate.opsForValue().get(cachedKey);
            if (cached != null) {
                log.debug("SystemConfig cache HIT: key='{}'", key);
                return cached;
            }
        } catch (Exception redisEx) {
            log.warn("Redis connection failed while reading key '{}'. Falling back to Database.", key);
        }

        log.debug("SystemConfig cache MISS: key='{}' — loading from DB", key);

        // 2. Try to read from Database (Gracefully handle missing keys or DB downtime)
        try {
            Optional<SystemConfig> dbConfig = configRepository.findByConfigKey(key);

            if (dbConfig.isPresent()) {
                String dbValue = dbConfig.get().getConfigValue();

                // 3. Try to save back to Redis (Gracefully handle Redis downtime)
                try {
                    redisTemplate.opsForValue().set(cachedKey, dbValue, CACHE_TTL);
                } catch (Exception redisEx) {
                    log.warn("Redis connection failed while writing key '{}'. Skipping cache update.", key);
                }

                return dbValue;
            }
        } catch (Exception dbEx) {
            log.error("Database connection failed while fetching config key '{}'.", key, dbEx);
        }

        // 4. Ultimate Fallback: Return the hardcoded default value safely
        log.warn("Config key '{}' missing or unreachable. Using ultimate fallback default: '{}'", key, defaultValue);
        return defaultValue;
    }

    public void evictCache(String key) {
        try {
            redisTemplate.delete(CACHE_PREFIX + key);
            log.info("SystemConfig cache evicted: key='{}'", key);
        } catch (Exception e) {
            log.warn("Failed to evict Redis cache for key '{}'. It may be down.", key);
        }
    }

    public void clearAllConfigCache() {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.warn("ADMIN ACTION: SystemConfig cache completely flushed. {} keys removed.", keys.size());
            } else {
                log.info("SystemConfig cache flush requested, but cache was already empty.");
            }
        } catch (Exception e) {
            log.warn("Failed to flush all Redis configurations. Redis may be down.", e);
        }
    }
}