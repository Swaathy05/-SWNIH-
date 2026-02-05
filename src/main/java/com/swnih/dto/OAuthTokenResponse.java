package com.swnih.dto;

import java.time.LocalDateTime;

/**
 * DTO for OAuth token response containing token information.
 */
public class OAuthTokenResponse {

    private boolean success;
    private String message;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    // Constructors
    public OAuthTokenResponse() {}

    public OAuthTokenResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public OAuthTokenResponse(boolean success, String message, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.success = success;
        this.message = message;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    // Static factory methods
    public static OAuthTokenResponse success(String message, LocalDateTime expiresAt, LocalDateTime createdAt) {
        return new OAuthTokenResponse(true, message, expiresAt, createdAt);
    }

    public static OAuthTokenResponse failure(String message) {
        return new OAuthTokenResponse(false, message);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OAuthTokenResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                '}';
    }
}