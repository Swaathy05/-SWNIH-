package com.swnih.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for OAuth authorization request containing the authorization URL.
 */
public class OAuthAuthorizationRequest {

    @NotBlank(message = "Authorization URL is required")
    private String authorizationUrl;

    @NotBlank(message = "State parameter is required")
    private String state;

    // Constructors
    public OAuthAuthorizationRequest() {}

    public OAuthAuthorizationRequest(String authorizationUrl, String state) {
        this.authorizationUrl = authorizationUrl;
        this.state = state;
    }

    // Getters and Setters
    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "OAuthAuthorizationRequest{" +
                "authorizationUrl='" + authorizationUrl + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}