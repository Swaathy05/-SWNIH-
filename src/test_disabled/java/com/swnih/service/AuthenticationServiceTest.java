package com.swnih.service;

import com.swnih.dto.AuthenticationResponse;
import com.swnih.dto.UserLoginRequest;
import com.swnih.dto.UserRegistrationRequest;
import com.swnih.entity.User;
import com.swnih.exception.AuthenticationException;
import com.swnih.exception.UserAlreadyExistsException;
import com.swnih.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService.
 * Tests user registration, login, and JWT token validation functionality.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserRegistrationRequest validRegistrationRequest;
    private UserLoginRequest validLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRegistrationRequest = new UserRegistrationRequest(
            "testuser",
            "test@example.com",
            "Password123"
        );

        validLoginRequest = new UserLoginRequest(
            "test@example.com",
            "Password123"
        );

        testUser = new User("testuser", "test@example.com", "hashedPassword");
        testUser.setId(1L);
    }

    @Test
    void registerUser_WithValidData_ShouldCreateUser() {
        // Given
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = authenticationService.registerUser(validRegistrationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authenticationService.registerUser(validRegistrationRequest))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("User already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithInvalidPassword_ShouldThrowException() {
        // Given
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "testuser",
            "test@example.com",
            "weak" // Too short, no uppercase, no digit
        );
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.registerUser(invalidRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password must be at least 8 characters long");
    }

    @Test
    void registerUser_WithPasswordMissingUppercase_ShouldThrowException() {
        // Given
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "testuser",
            "test@example.com",
            "password123" // No uppercase
        );
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.registerUser(invalidRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password must contain at least one uppercase letter");
    }

    @Test
    void registerUser_WithPasswordMissingLowercase_ShouldThrowException() {
        // Given
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "testuser",
            "test@example.com",
            "PASSWORD123" // No lowercase
        );
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.registerUser(invalidRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password must contain at least one lowercase letter");
    }

    @Test
    void registerUser_WithPasswordMissingDigit_ShouldThrowException() {
        // Given
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "testuser",
            "test@example.com",
            "Password" // No digit
        );
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.registerUser(invalidRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password must contain at least one numeric character");
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnToken() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenService.generateToken(1L, "test@example.com", "testuser")).thenReturn("jwt-token");

        // When
        AuthenticationResponse result = authenticationService.authenticateUser(validLoginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void authenticateUser_WithInvalidEmail_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticateUser(validLoginRequest))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("Invalid email or password");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenService, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void authenticateUser_WithInvalidPassword_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticateUser(validLoginRequest))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("Invalid email or password");

        verify(jwtTokenService, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnUser() {
        // Given
        String token = "valid-jwt-token";
        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.isTokenExpired(token)).thenReturn(false);
        when(jwtTokenService.getEmailFromToken(token)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User result = authenticationService.validateToken(token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void validateToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String token = "invalid-jwt-token";
        when(jwtTokenService.validateToken(token)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.validateToken(token))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("Invalid or expired token");
    }

    @Test
    void validateToken_WithExpiredToken_ShouldThrowException() {
        // Given
        String token = "expired-jwt-token";
        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.isTokenExpired(token)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authenticationService.validateToken(token))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("Token has expired");
    }

    @Test
    void userExistsByEmail_WhenUserExists_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = authenticationService.userExistsByEmail("test@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void userExistsByEmail_WhenUserDoesNotExist_ShouldReturnFalse() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        // When
        boolean result = authenticationService.userExistsByEmail("test@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User result = authenticationService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserByEmail_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.getUserByEmail("test@example.com"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("User not found with email");
    }
}