package com.swnih.integration;

import com.swnih.entity.OAuthToken;
import com.swnih.entity.User;
import com.swnih.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test demonstrating OAuth token entity working with encryption service.
 * Tests the complete flow of creating encrypted tokens and verifying encryption/decryption.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OAuth Token Integration Tests")
class OAuthTokenIntegrationTest {

    @Autowired
    private EncryptionService encryptionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedPassword");
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Should create OAuth token with encrypted access and refresh tokens")
    void shouldCreateOAuthTokenWithEncryptedTokens() {
        // Given
        String originalAccessToken = "ya29.a0AfH6SMBxyz123...original_access_token";
        String originalRefreshToken = "1//04abc123...original_refresh_token";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        // When - encrypt the tokens
        String encryptedAccessToken = encryptionService.encrypt(originalAccessToken);
        String encryptedRefreshToken = encryptionService.encrypt(originalRefreshToken);

        // Create OAuth token entity with encrypted tokens
        OAuthToken oauthToken = new OAuthToken(
            testUser,
            encryptedAccessToken,
            encryptedRefreshToken,
            expiresAt
        );

        // Then - verify the token was created correctly
        assertThat(oauthToken.getUser()).isEqualTo(testUser);
        assertThat(oauthToken.getAccessTokenEncrypted()).isEqualTo(encryptedAccessToken);
        assertThat(oauthToken.getRefreshTokenEncrypted()).isEqualTo(encryptedRefreshToken);
        assertThat(oauthToken.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(oauthToken.isExpired()).isFalse();

        // Verify tokens are encrypted (different from original)
        assertThat(oauthToken.getAccessTokenEncrypted()).isNotEqualTo(originalAccessToken);
        assertThat(oauthToken.getRefreshTokenEncrypted()).isNotEqualTo(originalRefreshToken);

        // Verify tokens can be decrypted back to original values
        String decryptedAccessToken = encryptionService.decrypt(oauthToken.getAccessTokenEncrypted());
        String decryptedRefreshToken = encryptionService.decrypt(oauthToken.getRefreshTokenEncrypted());

        assertThat(decryptedAccessToken).isEqualTo(originalAccessToken);
        assertThat(decryptedRefreshToken).isEqualTo(originalRefreshToken);
    }

    @Test
    @DisplayName("Should handle token expiration correctly")
    void shouldHandleTokenExpirationCorrectly() {
        // Given
        String accessToken = "access_token_123";
        String refreshToken = "refresh_token_456";
        
        // Create expired token
        LocalDateTime pastExpiration = LocalDateTime.now().minusHours(1);
        OAuthToken expiredToken = new OAuthToken(
            testUser,
            encryptionService.encrypt(accessToken),
            encryptionService.encrypt(refreshToken),
            pastExpiration
        );

        // Create valid token
        LocalDateTime futureExpiration = LocalDateTime.now().plusHours(1);
        OAuthToken validToken = new OAuthToken(
            testUser,
            encryptionService.encrypt(accessToken),
            encryptionService.encrypt(refreshToken),
            futureExpiration
        );

        // Then
        assertThat(expiredToken.isExpired()).isTrue();
        assertThat(validToken.isExpired()).isFalse();
        
        assertThat(expiredToken.getMinutesUntilExpiration()).isNegative();
        assertThat(validToken.getMinutesUntilExpiration()).isPositive();
    }

    @Test
    @DisplayName("Should handle token expiring soon detection")
    void shouldHandleTokenExpiringSoonDetection() {
        // Given
        String accessToken = "access_token_789";
        String refreshToken = "refresh_token_012";
        
        // Create token expiring in 5 minutes
        LocalDateTime soonExpiration = LocalDateTime.now().plusMinutes(5);
        OAuthToken soonExpiringToken = new OAuthToken(
            testUser,
            encryptionService.encrypt(accessToken),
            encryptionService.encrypt(refreshToken),
            soonExpiration
        );

        // Then
        assertThat(soonExpiringToken.isExpiringSoon(10)).isTrue(); // Within 10 minutes
        assertThat(soonExpiringToken.isExpiringSoon(2)).isFalse();  // Not within 2 minutes
        assertThat(soonExpiringToken.isExpired()).isFalse();        // Not yet expired
    }

    @Test
    @DisplayName("Should maintain user relationship correctly")
    void shouldMaintainUserRelationshipCorrectly() {
        // Given
        String accessToken = "user_relationship_access_token";
        String refreshToken = "user_relationship_refresh_token";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);

        // When
        OAuthToken oauthToken = new OAuthToken(
            testUser,
            encryptionService.encrypt(accessToken),
            encryptionService.encrypt(refreshToken),
            expiresAt
        );

        // Then
        assertThat(oauthToken.getUser()).isNotNull();
        assertThat(oauthToken.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(oauthToken.getUser().getUsername()).isEqualTo(testUser.getUsername());
        assertThat(oauthToken.getUser().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Should demonstrate complete OAuth token lifecycle")
    void shouldDemonstrateCompleteOAuthTokenLifecycle() {
        // Given - simulate OAuth flow response
        String gmailAccessToken = "ya29.a0AfH6SMC...gmail_access_token_from_google";
        String gmailRefreshToken = "1//04xyz789...gmail_refresh_token_from_google";
        LocalDateTime tokenExpiration = LocalDateTime.now().plusSeconds(3600); // 1 hour

        // When - store tokens securely
        String encryptedAccessToken = encryptionService.encrypt(gmailAccessToken);
        String encryptedRefreshToken = encryptionService.encrypt(gmailRefreshToken);

        OAuthToken storedToken = new OAuthToken(
            testUser,
            encryptedAccessToken,
            encryptedRefreshToken,
            tokenExpiration
        );

        // Then - verify secure storage
        assertThat(storedToken.getAccessTokenEncrypted()).isNotEqualTo(gmailAccessToken);
        assertThat(storedToken.getRefreshTokenEncrypted()).isNotEqualTo(gmailRefreshToken);

        // When - retrieve tokens for API calls
        String retrievedAccessToken = encryptionService.decrypt(storedToken.getAccessTokenEncrypted());
        String retrievedRefreshToken = encryptionService.decrypt(storedToken.getRefreshTokenEncrypted());

        // Then - verify tokens are correctly retrieved
        assertThat(retrievedAccessToken).isEqualTo(gmailAccessToken);
        assertThat(retrievedRefreshToken).isEqualTo(gmailRefreshToken);

        // Verify token is still valid
        assertThat(storedToken.isExpired()).isFalse();
        assertThat(storedToken.getMinutesUntilExpiration()).isBetween(59L, 60L);
    }

    @Test
    @DisplayName("Should validate encryption service configuration")
    void shouldValidateEncryptionServiceConfiguration() {
        // When
        boolean isConfigurationValid = encryptionService.validateConfiguration();

        // Then
        assertThat(isConfigurationValid).isTrue();
    }

    @Test
    @DisplayName("Should handle different token formats correctly")
    void shouldHandleDifferentTokenFormatsCorrectly() {
        // Given - various token formats that might come from OAuth providers
        String[] testTokens = {
            "ya29.a0AfH6SMC_short_token",
            "1//04very_long_refresh_token_with_many_characters_and_special_symbols_123456789",
            "simple_token",
            "token.with.dots.and-dashes_123",
            "token_with_unicode_chars_αβγδε_测试_🔐"
        };

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        for (String token : testTokens) {
            // When
            String encryptedToken = encryptionService.encrypt(token);
            OAuthToken oauthToken = new OAuthToken(
                testUser,
                encryptedToken,
                encryptionService.encrypt("refresh_" + token),
                expiresAt
            );

            // Then
            assertThat(oauthToken.getAccessTokenEncrypted()).isNotEqualTo(token);
            
            String decryptedToken = encryptionService.decrypt(oauthToken.getAccessTokenEncrypted());
            assertThat(decryptedToken).isEqualTo(token);
        }
    }
}