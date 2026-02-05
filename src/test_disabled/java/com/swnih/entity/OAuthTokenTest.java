package com.swnih.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for OAuthToken entity.
 * Tests entity behavior, validation, and helper methods.
 */
@DisplayName("OAuthToken Entity Tests")
class OAuthTokenTest {

    private User testUser;
    private OAuthToken oauthToken;
    private LocalDateTime futureExpiration;
    private LocalDateTime pastExpiration;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedPassword");
        testUser.setId(1L);
        
        futureExpiration = LocalDateTime.now().plusHours(1);
        pastExpiration = LocalDateTime.now().minusHours(1);
        
        oauthToken = new OAuthToken(
            testUser,
            "encryptedAccessToken",
            "encryptedRefreshToken",
            futureExpiration
        );
    }

    @Test
    @DisplayName("Should create OAuth token with all required fields")
    void shouldCreateOAuthTokenWithRequiredFields() {
        assertThat(oauthToken.getUser()).isEqualTo(testUser);
        assertThat(oauthToken.getAccessTokenEncrypted()).isEqualTo("encryptedAccessToken");
        assertThat(oauthToken.getRefreshTokenEncrypted()).isEqualTo("encryptedRefreshToken");
        assertThat(oauthToken.getExpiresAt()).isEqualTo(futureExpiration);
    }

    @Test
    @DisplayName("Should create OAuth token with default constructor")
    void shouldCreateOAuthTokenWithDefaultConstructor() {
        OAuthToken token = new OAuthToken();
        
        assertThat(token.getId()).isNull();
        assertThat(token.getUser()).isNull();
        assertThat(token.getAccessTokenEncrypted()).isNull();
        assertThat(token.getRefreshTokenEncrypted()).isNull();
        assertThat(token.getExpiresAt()).isNull();
    }

    @Test
    @DisplayName("Should correctly identify non-expired token")
    void shouldIdentifyNonExpiredToken() {
        assertThat(oauthToken.isExpired()).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify expired token")
    void shouldIdentifyExpiredToken() {
        oauthToken.setExpiresAt(pastExpiration);
        
        assertThat(oauthToken.isExpired()).isTrue();
    }

    @Test
    @DisplayName("Should correctly identify token expiring soon")
    void shouldIdentifyTokenExpiringSoon() {
        LocalDateTime soonExpiration = LocalDateTime.now().plusMinutes(5);
        oauthToken.setExpiresAt(soonExpiration);
        
        assertThat(oauthToken.isExpiringSoon(10)).isTrue();
        assertThat(oauthToken.isExpiringSoon(2)).isFalse();
    }

    @Test
    @DisplayName("Should calculate minutes until expiration correctly")
    void shouldCalculateMinutesUntilExpiration() {
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(30);
        oauthToken.setExpiresAt(expiration);
        
        long minutesUntilExpiration = oauthToken.getMinutesUntilExpiration();
        
        // Allow for small timing differences in test execution
        assertThat(minutesUntilExpiration).isBetween(29L, 30L);
    }

    @Test
    @DisplayName("Should return negative minutes for expired token")
    void shouldReturnNegativeMinutesForExpiredToken() {
        oauthToken.setExpiresAt(pastExpiration);
        
        long minutesUntilExpiration = oauthToken.getMinutesUntilExpiration();
        
        assertThat(minutesUntilExpiration).isNegative();
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        OAuthToken token1 = new OAuthToken();
        token1.setId(1L);
        
        OAuthToken token2 = new OAuthToken();
        token2.setId(1L);
        
        OAuthToken token3 = new OAuthToken();
        token3.setId(2L);
        
        OAuthToken tokenWithoutId = new OAuthToken();
        
        assertThat(token1).isEqualTo(token2);
        assertThat(token1).isNotEqualTo(token3);
        assertThat(token1).isNotEqualTo(tokenWithoutId);
        assertThat(tokenWithoutId).isNotEqualTo(token1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        OAuthToken token1 = new OAuthToken();
        OAuthToken token2 = new OAuthToken();
        
        assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
    }

    @Test
    @DisplayName("Should generate meaningful toString")
    void shouldGenerateMeaningfulToString() {
        oauthToken.setId(1L);
        
        String toString = oauthToken.toString();
        
        assertThat(toString).contains("OAuthToken");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("userId=1");
        assertThat(toString).contains("expiresAt=" + futureExpiration);
    }

    @Test
    @DisplayName("Should handle null user in toString")
    void shouldHandleNullUserInToString() {
        OAuthToken token = new OAuthToken();
        token.setId(1L);
        
        String toString = token.toString();
        
        assertThat(toString).contains("userId=null");
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        OAuthToken token = new OAuthToken();
        LocalDateTime now = LocalDateTime.now();
        
        token.setId(123L);
        token.setUser(testUser);
        token.setAccessTokenEncrypted("newAccessToken");
        token.setRefreshTokenEncrypted("newRefreshToken");
        token.setExpiresAt(now);
        token.setCreatedAt(now);
        token.setUpdatedAt(now);
        
        assertThat(token.getId()).isEqualTo(123L);
        assertThat(token.getUser()).isEqualTo(testUser);
        assertThat(token.getAccessTokenEncrypted()).isEqualTo("newAccessToken");
        assertThat(token.getRefreshTokenEncrypted()).isEqualTo("newRefreshToken");
        assertThat(token.getExpiresAt()).isEqualTo(now);
        assertThat(token.getCreatedAt()).isEqualTo(now);
        assertThat(token.getUpdatedAt()).isEqualTo(now);
    }
}