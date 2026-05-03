package com.onboardguard.shared.storage;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public interface CloudStorageService {

    String upload(String storageKey, MultipartFile file);

    String uploadBytes(String storageKey, byte[] content, String contentType);

    String generatePresignedUrl(String storageKey, Duration expiry, String contentType);

    void delete(String storageKey, String contentType);

    boolean exists(String storageKey, String contentType);
}