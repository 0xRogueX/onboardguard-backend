package com.onboardguard.shared.storage;

import lombok.Getter;

@Getter
public class CloudStorageException extends RuntimeException {

    private final String storageKey;

    public CloudStorageException(String message, String storageKey, Throwable cause) {
        super(message + " [Key: " + storageKey + "]", cause);
        this.storageKey = storageKey;
    }

}