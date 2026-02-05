package com.swnih.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swnih.dto.OAuthAuthorizationRequest;
import com.swnih.dto.OAuthCallbackRequest;
import com.swnih.dto.OAuthTokenResponse;
import com.swnih.entity.User;
import com.swnih.exception.OAuthException;
import com.swnih.service.AuthenticationService;
import com.swnih.service.GmailIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for GmailController.
 * Tests OAuth endpoints, error handling, and security integration.
 */
@WebMvcTest(GmailController.class)
class GmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GmailIntegrationService gmailIntegrationService;

    @MockBean
    private AuthenticationService authenticationService;

    private User testUser;
    private OAuthAuthorizationRequest authRequest;
    private OAuthTokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setId(1L);

        authRequest = new OAuthAuthorizationRequest(
                "https://accounts.google.com/o/oauth2/auth?client_id=test", 
                "test-state-123");

        tokenResponse = OAuthTokenResponse.success(
                "Gmail integration successful", 
                LocalDateTime.now().plusHours(1), 
                LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "testuser")
    void initiateGmailConnection_ShouldReturnAuthorizationUrl_WhenUserNotConnected() throws Exception {
        // Given
        when(authenticationService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(gmailIntegrationService.hasValidAuthorization(testUser)).thenReturn(false);
        when(gmailIntegrationService.initiateOAuthFlow(testUser)).thenReturn(authRequest);

        // When & Then
        mockMvc.perform(get("/api/gmail/connect")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.authorizationUrl").value(authRequest.getAuthorizationUrl()))
                .andExpect(jsonPath("$.state").value(authRequest.getState()));

        verify(gmailIntegrationService).initiateOAuthFlow(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void initiateGmailConnection_ShouldReturnAlreadyConnected_WhenUserHasValidAuth() throws Exception {
        // Given
        when(authenticationService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(gmailIntegrationService.hasValidAuthorization(testUser)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/gmail/connect")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Gmail is already connected"))
                .andExpect(jsonPath("$.alreadyConnected").value(true));

        verify(gmailIntegrationService, never()).initiateOAuthFlow(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void initiateGmailConnection_ShouldReturnError_WhenOAuthExceptionThrown() throws Exception {
        // Given
        when(authenticationService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(gmailIntegrationService.hasValidAuthorization(testUser)).thenReturn(false);
        when(gmailIntegrationService.initiateOAuthFlow(testUser))
                .thenThrow(new OAuthException("OAuth initiation failed", "OAUTH_ERROR"));

        // When & Then
        mockMvc.perform(get("/api/gmail/connect")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("OAUTH_ERROR"))
                .andExpect(jsonPath("$.message").value("OAuth initiation failed"));
    }

    @Test
    void initiateGmailConnection_ShouldReturnUnauthorized_WhenUserNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/gmail/connect"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void handleOAuthCallback_ShouldRedirectToDashboard_WhenCallbackSuccessful() throws Exception {
        // Given
        when(gmailIntegrationService.handleOAuthCallback(any(OAuthCallbackRequest.class)))
                .thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(get("/api/gmail/oauth/callback")
                .param("code", "test-auth-code")
                .param("state", "test-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/dashboard?gmail_connected=true*"));
    }

    @Test
    void handleOAuthCallback_ShouldRedirectWithError_WhenCallbackFails() throws Exception {
        // Given
        OAuthTokenResponse failureResponse = OAuthTokenResponse.failure("OAuth callback failed");
        when(gmailIntegrationService.handleOAuthCallback(any(OAuthCallbackRequest.class)))
                .thenReturn(failureResponse);

        // When & Then
        mockMvc.perform(get("/api/gmail/oauth/callback")
                .param("code", "test-auth-code")
                .param("state", "test-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/dashboard?gmail_connected=false*"));
    }

    @Test
    void handleOAuthCallback_ShouldRedirectWithError_WhenOAuthError() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/gmail/oauth/callback")
                .param("error", "access_denied")
                .param("error_description", "User denied access")
                .param("state", "test-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/dashboard?gmail_connected=false*"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void processOAuthCallback_ShouldReturnSuccess_WhenCallbackSuccessful() throws Exception {
        // Given
        OAuthCallbackRequest callbackRequest = new OAuthCallbackRequest("test-code", "test-state");
        when(gmailIntegrationService.handleOAuthCallback(any(OAuthCallbackRequest.class)))
                .thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/api/gmail/oauth/callback")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Gmail integration successful"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void processOAuthCallback_ShouldReturnError_WhenCallbackFails() throws Exception {
        // Given
        OAuthCallbackRequest callbackRequest = new OAuthCallbackRequest("test-code", "test-state");
        OAuthTokenResponse failureResponse = OAuthTokenResponse.failure("Invalid authorization code");
        when(gmailIntegrationService.handleOAuthCallback(any(OAuthCallbackRequest.class)))
                .thenReturn(failureResponse);

        // When & Then
        mockMvc.perform(post("/api/gmail/oauth/callback")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid authorization code"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getConnectionStatus_ShouldReturnConnected_WhenUserHasValidAuth() throws Exception {
        // Given
        when(authenticationService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(gmailIntegrationService.hasValidAuthorization(testUser)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/gmail/status")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.message").value("Gmail is connected"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getConnectionStatus_ShouldReturnNotConnected_WhenUserHasNoValidAuth() throws Exception {
        // Given
        when(authenticationService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(gmailIntegrationService.hasValidAuthorization(testUser)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/gmail/status")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.message").value("Gmail is not connected"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void disconnectGmail_ShouldReturnSuccess_WhenDisconnectionSuccessful() throws Exception {
        // Given
        when(authenticationService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(gmailIntegrationService).revokeAuthorization(testUser);

        // When & Then
        mockMvc.perform(delete("/api/gmail/disconnect")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Gmail has been disconnected successfully"));

        verify(gmailIntegrationService).revokeAuthorization(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void disconnectGmail_ShouldReturnError_WhenOAuthExceptionThrown() throws Exception {
        // Given
        when(authenticationService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        doThrow(new OAuthException("Revocation failed", "REVOCATION_ERROR"))
                .when(gmailIntegrationService).revokeAuthorization(testUser);

        // When & Then
        mockMvc.perform(delete("/api/gmail/disconnect")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("REVOCATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Revocation failed"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void refreshToken_ShouldReturnSuccess_WhenRefreshSuccessful() throws Exception {
        // Given
        when(authenticationService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(gmailIntegrationService.getValidAccessToken(testUser)).thenReturn("new-access-token");

        // When & Then
        mockMvc.perform(post("/api/gmail/refresh-token")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void refreshToken_ShouldReturnError_WhenRefreshFails() throws Exception {
        // Given
        when(authenticationService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(gmailIntegrationService.getValidAccessToken(testUser))
                .thenThrow(new OAuthException("Token refresh failed", "TOKEN_REFRESH_ERROR"));

        // When & Then
        mockMvc.perform(post("/api/gmail/refresh-token")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("TOKEN_REFRESH_ERROR"))
                .andExpect(jsonPath("$.message").value("Token refresh failed"));
    }

    @Test
    @WithMockUser(username = "nonexistentuser")
    void getCurrentUser_ShouldThrowException_WhenUserNotFound() throws Exception {
        // Given
        when(authenticationService.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/gmail/status")
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"));
    }

    // Validation tests

    @Test
    @WithMockUser(username = "testuser")
    void processOAuthCallback_ShouldReturnBadRequest_WhenRequestInvalid() throws Exception {
        // Given - invalid request with missing required fields
        OAuthCallbackRequest invalidRequest = new OAuthCallbackRequest();
        // code and state are null

        // When & Then
        mockMvc.perform(post("/api/gmail/oauth/callback")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleOAuthCallback_ShouldHandleExceptionGracefully() throws Exception {
        // Given
        when(gmailIntegrationService.handleOAuthCallback(any(OAuthCallbackRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(get("/api/gmail/oauth/callback")
                .param("code", "test-code")
                .param("state", "test-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/dashboard?gmail_connected=false*"));
    }
}