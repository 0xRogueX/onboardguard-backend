package com.onboardguard.shared.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't show 'data' if it's null
public class ApiResponse<T> {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private boolean success;
    private String message;
    private T data;

    // Helper method for quick success responses
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
}