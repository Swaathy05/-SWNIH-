package com.swnih.exception;

/**
 * Exception thrown when OAuth token refresh operations fail.
 */
public class TokenRefreshException extends RuntimeException {

    private final String errorCode;

    public TokenRefreshException(String message) {
        super(message);
        this.errorCode = "TOKEN_REFRESH_ERROR";
    }

    public TokenRefreshException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public TokenRefreshException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TOKEN_REFRESH_ERROR";
    }

    public TokenRefreshException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}