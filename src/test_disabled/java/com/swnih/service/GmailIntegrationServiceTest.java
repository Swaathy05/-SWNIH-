package com.swnih.service;

import com.swnih.dto.OAuthAuthorizationRequest;
import com.swnih.dto.OAuthCallbackRequest;
import com.swnih.dto.OAuthTokenResponse;
import com.swnih.entity.OAuthToken;
import com.swnih.entity.User;
import com.swnih.exception.OAuthException;
import com.swnih.exception.TokenRefreshException;
import com.swnih.repository.OAuthTokenRepository;
import com.swnih.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GmailIntegrationService.
 * Tests OAuth flow initiation, callback handling, token management, and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
class GmailIntegrationServiceTest {

    @Mock
    private OAuthTokenRepository oauthTokenRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private UserRepository userRepository;

    private GmailIntegrationService gmailIntegrationService;
    private User testUser;
    private OAuthToken testToken;

    @BeforeEach
    void setUp() throws Exception {
        gmailIntegrationService = new GmailIntegrationService(
                oauthTokenRepository, encryptionService, userRepository);

        // Set up test configuration using reflection
        ReflectionTestUtils.setField(gmailIntegrationService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(gmailIntegrationService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(gmailIntegrationService, "redirectUri", "http://localhost:8080/api/gmail/oauth/callback");

        // Create test user
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setId(1L);

        // Create test OAuth token
        testToken = new OAuthToken(testUser, "encrypted-access-token", "encrypted-refresh-token", 
                                 LocalDateTime.now().plusHours(1));
        testToken.setId(1L);
    }

    @Test
    void initiateOAuthFlow_ShouldReturnAuthorizationRequest_WhenValidUser() {
        // When
        OAuthAuthorizationRequest result = gmailIntegrationService.initiateOAuthFlow(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthorizationUrl()).isNotNull();
        assertThat(result.getAuthorizationUrl()).contains("accounts.google.com/o/oauth2/auth");
        assertThat(result.getState()).isNotNull();
        assertThat(result.getState()).hasSize(36); // UUID length
    }

    @Test
    void initiateOAuthFlow_ShouldThrowException_WhenUserIsNull() {
        // When & Then
        assertThatThrownBy(() -> gmailIntegrationService.initiateOAuthFlow(null))
                .isInstanceOf(OAuthException.class)
                .hasMessageContaining("User cannot be null");
    }

    @Test
    void handleOAuthCallback_ShouldReturnFailure_WhenCallbackHasError() {
        // Given
        OAuthCallbackRequest callbackRequest = new OAuthCallbackRequest();
        callbackRequest.setError("access_denied");
        callbackRequest.setErrorDescription("User denied access");
        callbackRequest.setState("test-state");

        // When
        OAuthTokenResponse result = gmailIntegrationService.handleOAuthCallback(callbackRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("OAuth authorization failed");
    }

    @Test
    void handleOAuthCallback_ShouldReturnFailure_WhenStateIsInvalid() {
        // Given
        OAuthCallbackRequest callbackRequest = new OAuthCallbackRequest();
        callbackRequest.setCode("test-auth-code");
        callbackRequest.setState("invalid-state");

        // When
        OAuthTokenResponse result = gmailIntegrationService.handleOAuthCallback(callbackRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Invalid or expired authorization request");
    }

    @Test
    void getValidAccessToken_ShouldReturnDecryptedToken_WhenValidTokenExists() {
        // Given
        when(oauthTokenRepository.findValidTokenByUser(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testToken));
        when(encryptionService.decrypt("encrypted-access-token"))
                .thenReturn("decrypted-access-token");

        // When
        String result = gmailIntegrationService.getValidAccessToken(testUser);

        // Then
        assertThat(result).isEqualTo("decrypted-access-token");
        verify(encryptionService).decrypt("encrypted-access-token");
    }

    @Test
    void getValidAccessToken_ShouldThrowException_WhenNoValidTokenExists() {
        // Given
        when(oauthTokenRepository.findValidTokenByUser(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gmailIntegrationService.getValidAccessToken(testUser))
                .isInstanceOf(OAuthException.class)
                .hasMessageContaining("No valid Gmail authorization found");
    }

    @Test
    void getValidAccessToken_ShouldReturnToken_WhenTokenNotExpiringSoon() {
        // Given - token expires in 10 minutes (not expiring soon)
        OAuthToken validToken = new OAuthToken(testUser, "encrypted-access-token", 
                                             "encrypted-refresh-token", 
                                             LocalDateTime.now().plusMinutes(10));
        validToken.setId(1L);

        when(oauthTokenRepository.findValidTokenByUser(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(Optional.of(validToken));
        when(encryptionService.decrypt("encrypted-access-token"))
                .thenReturn("decrypted-access-token");

        // When
        String result = gmailIntegrationService.getValidAccessToken(testUser);

        // Then
        assertThat(result).isEqualTo("decrypted-access-token");
        verify(encryptionService).decrypt("encrypted-access-token");
        // Should not attempt to refresh since token is not expiring soon
        verify(oauthTokenRepository, never()).save(any(OAuthToken.class));
    }

    @Test
    void hasValidAuthorization_ShouldReturnTrue_WhenValidTokenExists() {
        // Given
        when(oauthTokenRepository.hasValidToken(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(true);

        // When
        boolean result = gmailIntegrationService.hasValidAuthorization(testUser);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasValidAuthorization_ShouldReturnFalse_WhenNoValidTokenExists() {
        // Given
        when(oauthTokenRepository.hasValidToken(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(false);

        // When
        boolean result = gmailIntegrationService.hasValidAuthorization(testUser);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasValidAuthorization_ShouldReturnFalse_WhenExceptionOccurs() {
        // Given
        when(oauthTokenRepository.hasValidToken(eq(testUser), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = gmailIntegrationService.hasValidAuthorization(testUser);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void revokeAuthorization_ShouldDeleteTokens_WhenCalled() {
        // Given
        when(oauthTokenRepository.deleteByUser(testUser)).thenReturn(2L);

        // When
        gmailIntegrationService.revokeAuthorization(testUser);

        // Then
        verify(oauthTokenRepository).deleteByUser(testUser);
    }

    @Test
    void revokeAuthorization_ShouldThrowException_WhenDeletionFails() {
        // Given
        when(oauthTokenRepository.deleteByUser(testUser))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> gmailIntegrationService.revokeAuthorization(testUser))
                .isInstanceOf(OAuthException.class)
                .hasMessageContaining("Failed to revoke Gmail authorization");
    }

    @Test
    void createGmailClient_ShouldReturnGmailClient_WhenValidTokenExists() {
        // Given
        when(oauthTokenRepository.findValidTokenByUser(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testToken));
        when(encryptionService.decrypt("encrypted-access-token"))
                .thenReturn("valid-access-token");

        // When
        com.google.api.services.gmail.Gmail result = gmailIntegrationService.createGmailClient(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getApplicationName()).isEqualTo("Smart Web Notification Intelligence Hub");
    }

    @Test
    void createGmailClient_ShouldThrowException_WhenNoValidTokenExists() {
        // Given
        when(oauthTokenRepository.findValidTokenByUser(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gmailIntegrationService.createGmailClient(testUser))
                .isInstanceOf(OAuthException.class)
                .hasMessageContaining("No valid Gmail authorization found");
    }

    @Test
    void refreshAccessToken_ShouldThrowException_WhenRefreshFails() {
        // Given - we'll test this by mocking the encryption service to fail
        when(encryptionService.decrypt("encrypted-refresh-token"))
                .thenThrow(new RuntimeException("Decryption failed"));

        // When & Then
        assertThatThrownBy(() -> gmailIntegrationService.refreshAccessToken(testToken))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Failed to refresh access token");
    }

    // Edge case tests

    @Test
    void initiateOAuthFlow_ShouldHandleMultipleConcurrentRequests() {
        // Given
        User user1 = new User("user1", "user1@example.com", "hash1");
        user1.setId(1L);
        User user2 = new User("user2", "user2@example.com", "hash2");
        user2.setId(2L);

        // When
        OAuthAuthorizationRequest result1 = gmailIntegrationService.initiateOAuthFlow(user1);
        OAuthAuthorizationRequest result2 = gmailIntegrationService.initiateOAuthFlow(user2);

        // Then
        assertThat(result1.getState()).isNotEqualTo(result2.getState());
        assertThat(result1.getAuthorizationUrl()).isNotEqualTo(result2.getAuthorizationUrl());
    }

    @Test
    void getValidAccessToken_ShouldHandleEncryptionFailure() {
        // Given
        when(oauthTokenRepository.findValidTokenByUser(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testToken));
        when(encryptionService.decrypt("encrypted-access-token"))
                .thenThrow(new RuntimeException("Decryption failed"));

        // When & Then
        assertThatThrownBy(() -> gmailIntegrationService.getValidAccessToken(testUser))
                .isInstanceOf(OAuthException.class)
                .hasMessageContaining("Failed to get valid access token");
    }

    @Test
    void handleOAuthCallback_ShouldReturnFailure_WhenNullParameters() {
        // Given
        OAuthCallbackRequest callbackRequest = new OAuthCallbackRequest();
        callbackRequest.setCode(null);
        callbackRequest.setState(null);

        // When
        OAuthTokenResponse result = gmailIntegrationService.handleOAuthCallback(callbackRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Invalid or expired authorization request");
    }
}