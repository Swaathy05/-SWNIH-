package com.swnih.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for OAuth callback request containing authorization code and state.
 */
public class OAuthCallbackRequest {

    @NotBlank(message = "Authorization code is required")
    private String code;

    @NotBlank(message = "State parameter is required")
    private String state;

    private String error;
    private String errorDescription;

    // Constructors
    public OAuthCallbackRequest() {}

    public OAuthCallbackRequest(String code, String state) {
        this.code = code;
        this.state = state;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public boolean hasError() {
        return error != null && !error.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "OAuthCallbackRequest{" +
                "code='" + (code != null ? "[REDACTED]" : null) + '\'' +
                ", state='" + state + '\'' +
                ", error='" + error + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }
}