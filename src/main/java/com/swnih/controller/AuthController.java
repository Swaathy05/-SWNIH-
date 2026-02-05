package com.swnih.controller;

import com.swnih.dto.AuthenticationResponse;
import com.swnih.dto.UserLoginRequest;
import com.swnih.dto.UserRegistrationRequest;
import com.swnih.entity.User;
import com.swnih.exception.AuthenticationException;
import com.swnih.exception.UserAlreadyExistsException;
import com.swnih.service.AuthenticationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for user authentication endpoints.
 * Handles user registration, login, and JWT token management.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Register a new user account.
     * 
     * @param registrationRequest user registration data
     * @return registration result with user information
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        try {
            logger.info("Attempting to register user with email: {}", registrationRequest.getEmail());

            User user = authenticationService.registerUser(registrationRequest);

            logger.info("Successfully registered user with ID: {} and email: {}", 
                       user.getId(), user.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "success", true,
                        "message", "User registered successfully",
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail()
                    ));

        } catch (UserAlreadyExistsException e) {
            logger.warn("Registration failed - user already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                        "success", false,
                        "error", "USER_ALREADY_EXISTS",
                        "message", e.getMessage()
                    ));
        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed - invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "error", "INVALID_INPUT",
                        "message", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Unexpected error during user registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "An unexpected error occurred during registration"
                    ));
        }
    }

    /**
     * Authenticate user and generate JWT token.
     * 
     * @param loginRequest user login credentials
     * @return authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest loginRequest) {
        try {
            logger.info("Attempting to authenticate user with email: {}", loginRequest.getEmail());

            AuthenticationResponse authResponse = authenticationService.authenticateUser(loginRequest);

            logger.info("Successfully authenticated user: {}", authResponse.getUsername());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "token", authResponse.getToken(),
                "tokenType", authResponse.getTokenType(),
                "userId", authResponse.getUserId(),
                "username", authResponse.getUsername(),
                "email", authResponse.getEmail()
            ));

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "error", "AUTHENTICATION_FAILED",
                        "message", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Unexpected error during user authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "An unexpected error occurred during login"
                    ));
        }
    }

    /**
     * Validate JWT token and return user information.
     * 
     * @param authHeader Authorization header containing JWT token
     * @return user information if token is valid
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                            "success", false,
                            "error", "INVALID_TOKEN_FORMAT",
                            "message", "Authorization header must contain Bearer token"
                        ));
            }

            String token = authHeader.substring(7);
            User user = authenticationService.validateToken(token);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token is valid",
                "userId", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
            ));

        } catch (AuthenticationException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "error", "INVALID_TOKEN",
                        "message", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Unexpected error during token validation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "An unexpected error occurred during token validation"
                    ));
        }
    }

    /**
     * Get current user information from JWT token.
     * 
     * @param authHeader Authorization header containing JWT token
     * @return current user information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                            "success", false,
                            "error", "INVALID_TOKEN_FORMAT",
                            "message", "Authorization header must contain Bearer token"
                        ));
            }

            String token = authHeader.substring(7);
            User user = authenticationService.validateToken(token);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "createdAt", user.getCreatedAt()
                )
            ));

        } catch (AuthenticationException e) {
            logger.warn("Failed to get current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "error", "INVALID_TOKEN",
                        "message", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Unexpected error getting current user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "An unexpected error occurred"
                    ));
        }
    }
}