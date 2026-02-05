package com.swnih.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for User entity validation and functionality.
 */
@DisplayName("User Entity Tests")
class UserEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid user with all required fields")
    void shouldCreateValidUser() {
        // Given
        User user = new User("testuser", "test@example.com", "hashedPassword123");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashedPassword123");
    }

    @Test
    @DisplayName("Should fail validation when username is blank")
    void shouldFailValidationWhenUsernameIsBlank() {
        // Given
        User user = new User("", "test@example.com", "hashedPassword123");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Username is required"));
    }

    @Test
    @DisplayName("Should fail validation when username is too short")
    void shouldFailValidationWhenUsernameIsTooShort() {
        // Given
        User user = new User("ab", "test@example.com", "hashedPassword123");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("Should fail validation when username is too long")
    void shouldFailValidationWhenUsernameIsTooLong() {
        // Given
        String longUsername = "a".repeat(51);
        User user = new User(longUsername, "test@example.com", "hashedPassword123");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("Should fail validation when email is blank")
    void shouldFailValidationWhenEmailIsBlank() {
        // Given
        User user = new User("testuser", "", "hashedPassword123");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Email is required");
    }

    @Test
    @DisplayName("Should fail validation when email format is invalid")
    void shouldFailValidationWhenEmailFormatIsInvalid() {
        // Given
        User user = new User("testuser", "invalid-email", "hashedPassword123");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Email must be valid");
    }

    @Test
    @DisplayName("Should fail validation when email is too long")
    void shouldFailValidationWhenEmailIsTooLong() {
        // Given
        String longEmail = "a".repeat(90) + "@example.com"; // > 100 chars
        User user = new User("testuser", longEmail, "hashedPassword123");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Email must not exceed 100 characters"));
    }

    @Test
    @DisplayName("Should fail validation when password hash is blank")
    void shouldFailValidationWhenPasswordHashIsBlank() {
        // Given
        User user = new User("testuser", "test@example.com", "");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Password hash is required");
    }

    @Test
    @DisplayName("Should properly manage message relationships")
    void shouldProperlyManageMessageRelationships() {
        // Given
        User user = new User("testuser", "test@example.com", "hashedPassword123");
        Message message = new Message();
        message.setSender("sender@example.com");
        message.setSubject("Test Subject");
        message.setBody("Test Body");

        // When
        user.addMessage(message);

        // Then
        assertThat(user.getMessages()).hasSize(1);
        assertThat(user.getMessages().get(0)).isEqualTo(message);
        assertThat(message.getUser()).isEqualTo(user);

        // When removing
        user.removeMessage(message);

        // Then
        assertThat(user.getMessages()).isEmpty();
        assertThat(message.getUser()).isNull();
    }

    @Test
    @DisplayName("Should properly manage OAuth token relationships")
    void shouldProperlyManageOAuthTokenRelationships() {
        // Given
        User user = new User("testuser", "test@example.com", "hashedPassword123");
        OAuthToken token = new OAuthToken();
        token.setAccessTokenEncrypted("encrypted_access_token");
        token.setRefreshTokenEncrypted("encrypted_refresh_token");

        // When
        user.addOAuthToken(token);

        // Then
        assertThat(user.getOauthTokens()).hasSize(1);
        assertThat(user.getOauthTokens().get(0)).isEqualTo(token);
        assertThat(token.getUser()).isEqualTo(user);

        // When removing
        user.removeOAuthToken(token);

        // Then
        assertThat(user.getOauthTokens()).isEmpty();
        assertThat(token.getUser()).isNull();
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        User user1 = new User("testuser", "test@example.com", "hashedPassword123");
        user1.setId(1L);
        
        User user2 = new User("testuser", "test@example.com", "hashedPassword123");
        user2.setId(1L);
        
        User user3 = new User("testuser", "test@example.com", "hashedPassword123");
        user3.setId(2L);

        // Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("Should generate meaningful toString representation")
    void shouldGenerateMeaningfulToStringRepresentation() {
        // Given
        User user = new User("testuser", "test@example.com", "hashedPassword123");
        user.setId(1L);

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).contains("User{");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("username='testuser'");
        assertThat(toString).contains("email='test@example.com'");
        assertThat(toString).doesNotContain("hashedPassword123"); // Should not expose password
    }
}