package com.swnih.exception;

/**
 * Exception thrown when OAuth operations fail.
 */
public class OAuthException extends RuntimeException {

    private final String errorCode;

    public OAuthException(String message) {
        super(message);
        this.errorCode = "OAUTH_ERROR";
    }

    public OAuthException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public OAuthException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "OAUTH_ERROR";
    }

    public OAuthException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}