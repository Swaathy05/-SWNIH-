package com.swnih.service;

import com.swnih.dto.AuthenticationResponse;
import com.swnih.dto.UserLoginRequest;
import com.swnih.dto.UserRegistrationRequest;
import com.swnih.entity.User;
import com.swnih.exception.AuthenticationException;
import com.swnih.exception.UserAlreadyExistsException;
import com.swnih.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for AuthenticationService.
 * Tests the complete authentication flow with real database and Spring context.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthenticationIntegrationTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void completeAuthenticationFlow_ShouldWorkEndToEnd() {
        // Given
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
            "integrationuser",
            "integration@example.com",
            "IntegrationTest123"
        );

        // When - Register user
        User registeredUser = authenticationService.registerUser(registrationRequest);

        // Then - Verify registration
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getId()).isNotNull();
        assertThat(registeredUser.getUsername()).isEqualTo("integrationuser");
        assertThat(registeredUser.getEmail()).isEqualTo("integration@example.com");
        assertThat(registeredUser.getPasswordHash()).isNotEqualTo("IntegrationTest123"); // Should be hashed
        assertThat(passwordEncoder.matches("IntegrationTest123", registeredUser.getPasswordHash())).isTrue();

        // When - Login with correct credentials
        UserLoginRequest loginRequest = new UserLoginRequest(
            "integration@example.com",
            "IntegrationTest123"
        );
        AuthenticationResponse authResponse = authenticationService.authenticateUser(loginRequest);

        // Then - Verify login response
        assertThat(authResponse).isNotNull();
        assertThat(authResponse.getToken()).isNotNull();
        assertThat(authResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(authResponse.getUserId()).isEqualTo(registeredUser.getId());
        assertThat(authResponse.getUsername()).isEqualTo("integrationuser");
        assertThat(authResponse.getEmail()).isEqualTo("integration@example.com");

        // When - Validate token
        User validatedUser = authenticationService.validateToken(authResponse.getToken());

        // Then - Verify token validation
        assertThat(validatedUser).isNotNull();
        assertThat(validatedUser.getId()).isEqualTo(registeredUser.getId());
        assertThat(validatedUser.getEmail()).isEqualTo("integration@example.com");

        // Verify JWT token claims
        assertThat(jwtTokenService.getEmailFromToken(authResponse.getToken())).isEqualTo("integration@example.com");
        assertThat(jwtTokenService.getUserIdFromToken(authResponse.getToken())).isEqualTo(registeredUser.getId());
        assertThat(jwtTokenService.getUsernameFromToken(authResponse.getToken())).isEqualTo("integrationuser");
        assertThat(jwtTokenService.validateToken(authResponse.getToken())).isTrue();
        assertThat(jwtTokenService.isTokenExpired(authResponse.getToken())).isFalse();
    }

    @Test
    void registerUser_WithBcryptHashing_ShouldUse12Rounds() {
        // Given
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
            "bcryptuser",
            "bcrypt@example.com",
            "BcryptTest123"
        );

        // When
        User registeredUser = authenticationService.registerUser(registrationRequest);

        // Then - Verify bcrypt hash format (should start with $2a$12$ for 12 rounds)
        assertThat(registeredUser.getPasswordHash()).startsWith("$2a$12$");
        assertThat(passwordEncoder.matches("BcryptTest123", registeredUser.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("WrongPassword", registeredUser.getPasswordHash())).isFalse();
    }

    @Test
    void registerUser_WithDuplicateEmail_ShouldThrowException() {
        // Given - Register first user
        UserRegistrationRequest firstRequest = new UserRegistrationRequest(
            "firstuser",
            "duplicate@example.com",
            "FirstUser123"
        );
        authenticationService.registerUser(firstRequest);

        // When - Try to register second user with same email
        UserRegistrationRequest duplicateRequest = new UserRegistrationRequest(
            "seconduser",
            "duplicate@example.com",
            "SecondUser123"
        );

        // Then
        assertThatThrownBy(() -> authenticationService.registerUser(duplicateRequest))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("User already exists");
    }

    @Test
    void registerUser_WithDuplicateUsername_ShouldThrowException() {
        // Given - Register first user
        UserRegistrationRequest firstRequest = new UserRegistrationRequest(
            "duplicateuser",
            "first@example.com",
            "FirstUser123"
        );
        authenticationService.registerUser(firstRequest);

        // When - Try to register second user with same username
        UserRegistrationRequest duplicateRequest = new UserRegistrationRequest(
            "duplicateuser",
            "second@example.com",
            "SecondUser123"
        );

        // Then
        assertThatThrownBy(() -> authenticationService.registerUser(duplicateRequest))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("User already exists");
    }

    @Test
    void authenticateUser_WithWrongPassword_ShouldThrowException() {
        // Given - Register user
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
            "wrongpassuser",
            "wrongpass@example.com",
            "CorrectPassword123"
        );
        authenticationService.registerUser(registrationRequest);

        // When - Try to login with wrong password
        UserLoginRequest loginRequest = new UserLoginRequest(
            "wrongpass@example.com",
            "WrongPassword123"
        );

        // Then
        assertThatThrownBy(() -> authenticationService.authenticateUser(loginRequest))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("Invalid email or password");
    }

    @Test
    void authenticateUser_WithNonexistentEmail_ShouldThrowException() {
        // When - Try to login with non-existent email
        UserLoginRequest loginRequest = new UserLoginRequest(
            "nonexistent@example.com",
            "SomePassword123"
        );

        // Then
        assertThatThrownBy(() -> authenticationService.authenticateUser(loginRequest))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("Invalid email or password");
    }

    @Test
    void passwordValidation_ShouldEnforceAllRequirements() {
        // Test various invalid passwords
        String[] invalidPasswords = {
            "short",                    // Too short
            "nouppercase123",          // No uppercase
            "NOLOWERCASE123",          // No lowercase
            "NoNumbers",               // No digits
            "Valid1"                   // Too short but otherwise valid
        };

        for (String invalidPassword : invalidPasswords) {
            UserRegistrationRequest request = new UserRegistrationRequest(
                "testuser" + System.nanoTime(),
                "test" + System.nanoTime() + "@example.com",
                invalidPassword
            );

            assertThatThrownBy(() -> authenticationService.registerUser(request))
                .isInstanceOf(IllegalArgumentException.class);
        }

        // Test valid password
        UserRegistrationRequest validRequest = new UserRegistrationRequest(
            "validuser",
            "valid@example.com",
            "ValidPassword123"
        );

        assertThatCode(() -> authenticationService.registerUser(validRequest))
            .doesNotThrowAnyException();
    }

    @Test
    void jwtToken_ShouldHave24HourExpiration() {
        // Given
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
            "tokenuser",
            "token@example.com",
            "TokenTest123"
        );
        User user = authenticationService.registerUser(registrationRequest);

        UserLoginRequest loginRequest = new UserLoginRequest(
            "token@example.com",
            "TokenTest123"
        );

        // When
        AuthenticationResponse authResponse = authenticationService.authenticateUser(loginRequest);

        // Then - Verify token expiration is approximately 24 hours
        long expirationMs = jwtTokenService.getExpirationMs();
        assertThat(expirationMs).isEqualTo(86400000L); // 24 hours in milliseconds

        // Verify token expiration date
        java.util.Date expirationDate = jwtTokenService.getExpirationDateFromToken(authResponse.getToken());
        java.util.Date now = new java.util.Date();
        long timeDifference = expirationDate.getTime() - now.getTime();
        
        // Should be approximately 24 hours (allowing for small test execution time)
        assertThat(timeDifference).isBetween(86390000L, 86400000L); // 23h 59m 50s to 24h
    }
}