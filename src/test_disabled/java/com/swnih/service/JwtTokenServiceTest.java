package com.swnih.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtTokenService.
 * Tests JWT token generation, validation, and claim extraction.
 */
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtTokenService, "jwtSecret", "test-jwt-secret-key-should-be-at-least-256-bits-long-for-security");
        ReflectionTestUtils.setField(jwtTokenService, "jwtExpirationMs", 86400000L); // 24 hours
    }

    @Test
    void generateToken_WithValidData_ShouldCreateValidToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String username = "testuser";

        // When
        String token = jwtTokenService.generateToken(userId, email, username);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenService.validateToken(token)).isTrue();
    }

    @Test
    void getEmailFromToken_WithValidToken_ShouldReturnEmail() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String username = "testuser";
        String token = jwtTokenService.generateToken(userId, email, username);

        // When
        String extractedEmail = jwtTokenService.getEmailFromToken(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void getUserIdFromToken_WithValidToken_ShouldReturnUserId() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String username = "testuser";
        String token = jwtTokenService.generateToken(userId, email, username);

        // When
        Long extractedUserId = jwtTokenService.getUserIdFromToken(token);

        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void getUsernameFromToken_WithValidToken_ShouldReturnUsername() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String username = "testuser";
        String token = jwtTokenService.generateToken(userId, email, username);

        // When
        String extractedUsername = jwtTokenService.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void getExpirationDateFromToken_WithValidToken_ShouldReturnFutureDate() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String username = "testuser";
        String token = jwtTokenService.generateToken(userId, email, username);

        // When
        Date expirationDate = jwtTokenService.getExpirationDateFromToken(token);

        // Then
        assertThat(expirationDate).isAfter(new Date());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String username = "testuser";
        String token = jwtTokenService.generateToken(userId, email, username);

        // When
        boolean isValid = jwtTokenService.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtTokenService.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtTokenService.validateToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtTokenService.validateToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String username = "testuser";
        String token = jwtTokenService.generateToken(userId, email, username);

        // When
        boolean isExpired = jwtTokenService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    void isTokenExpired_WithExpiredToken_ShouldReturnTrue() {
        // Given - Create service with very short expiration
        JwtTokenService shortExpirationService = new JwtTokenService();
        ReflectionTestUtils.setField(shortExpirationService, "jwtSecret", "test-jwt-secret-key-should-be-at-least-256-bits-long-for-security");
        ReflectionTestUtils.setField(shortExpirationService, "jwtExpirationMs", 1L); // 1 millisecond

        String token = shortExpirationService.generateToken(1L, "test@example.com", "testuser");

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isExpired = shortExpirationService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    void isTokenExpired_WithInvalidToken_ShouldReturnTrue() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isExpired = jwtTokenService.isTokenExpired(invalidToken);

        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    void getExpirationMs_ShouldReturnConfiguredValue() {
        // When
        long expirationMs = jwtTokenService.getExpirationMs();

        // Then
        assertThat(expirationMs).isEqualTo(86400000L); // 24 hours
    }

    @Test
    void getEmailFromToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtTokenService.getEmailFromToken(invalidToken))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void getUserIdFromToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtTokenService.getUserIdFromToken(invalidToken))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void getUsernameFromToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtTokenService.getUsernameFromToken(invalidToken))
            .isInstanceOf(JwtException.class);
    }
}