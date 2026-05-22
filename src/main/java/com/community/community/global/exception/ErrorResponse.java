package com.community.community.global.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        String message,
        LocalDateTime timestamp,
        Map<String, String> details
) {
    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, LocalDateTime.now(), Map.of());
    }

    public static ErrorResponse of(String message, Map<String, String> details) {
        return new ErrorResponse(message, LocalDateTime.now(), details);
    }
}
