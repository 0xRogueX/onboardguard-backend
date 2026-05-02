package com.onboardguard.shared.storage;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ObjectStorageConfig {

    @Value("${app.storage.cloudinary.cloud-name}")
    private String cloudName;

    @Value("${app.storage.cloudinary.api-key}")
    private String apiKey;

    @Value("${app.storage.cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        if (!StringUtils.hasText(cloudName) || !StringUtils.hasText(apiKey) || !StringUtils.hasText(apiSecret)) {
            throw new IllegalStateException(
                "Cloudinary configuration is missing. " +
                "Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET in your .env or application.yaml"
            );
        }

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key",    apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure",     true);
        config.put("connect_timeout", 10000);
        config.put("read_timeout",    30000);
        config.put("upload_timeout",  60000);

        return new Cloudinary(config);
    }
}
