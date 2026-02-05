package com.swnih.service;

import com.swnih.dto.AuthenticationResponse;
import com.swnih.dto.UserLoginRequest;
import com.swnih.dto.UserRegistrationRequest;
import com.swnih.entity.User;
import com.swnih.exception.AuthenticationException;
import com.swnih.exception.UserAlreadyExistsException;
import com.swnih.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for user authentication operations.
 * Handles user registration, login, and JWT token generation.
 * Implements bcrypt password hashing with 12+ rounds as per requirements.
 */
@Service
@Transactional
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Autowired
    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Register a new user with password validation and bcrypt hashing.
     * 
     * @param registrationRequest user registration data
     * @return created user entity
     * @throws UserAlreadyExistsException if username or email already exists
     * @throws IllegalArgumentException if password validation fails
     */
    public User registerUser(UserRegistrationRequest registrationRequest) {
        logger.info("Attempting to register user with email: {}", registrationRequest.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmailOrUsername(registrationRequest.getEmail(), registrationRequest.getUsername())) {
            String message = "User already exists with email or username";
            logger.warn("Registration failed: {} - Email: {}, Username: {}", 
                       message, registrationRequest.getEmail(), registrationRequest.getUsername());
            throw new UserAlreadyExistsException(message);
        }

        // Validate password requirements (additional validation beyond annotations)
        validatePasswordRequirements(registrationRequest.getPassword());

        // Hash password with bcrypt (12+ rounds configured in SecurityConfig)
        String hashedPassword = passwordEncoder.encode(registrationRequest.getPassword());

        // Create new user
        User user = new User(
            registrationRequest.getUsername(),
            registrationRequest.getEmail(),
            hashedPassword
        );

        // Save user to database
        User savedUser = userRepository.save(user);
        logger.info("Successfully registered user with ID: {} and email: {}", 
                   savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * Authenticate user and generate JWT token.
     * 
     * @param loginRequest user login credentials
     * @return authentication response with JWT token
     * @throws AuthenticationException if credentials are invalid
     */
    public AuthenticationResponse authenticateUser(UserLoginRequest loginRequest) {
        logger.info("Attempting to authenticate user with email: {}", loginRequest.getEmail());

        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if (userOptional.isEmpty()) {
            logger.warn("Authentication failed: User not found with email: {}", loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        User user = userOptional.get();

        // Validate password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            logger.warn("Authentication failed: Invalid password for user: {}", loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtTokenService.generateToken(user.getId(), user.getEmail(), user.getUsername());

        logger.info("Successfully authenticated user with ID: {} and email: {}", 
                   user.getId(), user.getEmail());

        return new AuthenticationResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }

    /**
     * Validate JWT token and return user information.
     * 
     * @param token JWT token
     * @return user entity if token is valid
     * @throws AuthenticationException if token is invalid or expired
     */
    public User validateToken(String token) {
        if (!jwtTokenService.validateToken(token)) {
            throw new AuthenticationException("Invalid or expired token");
        }

        if (jwtTokenService.isTokenExpired(token)) {
            throw new AuthenticationException("Token has expired");
        }

        String email = jwtTokenService.getEmailFromToken(token);
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            throw new AuthenticationException("User not found for token");
        }

        return userOptional.get();
    }

    /**
     * Check if user exists by email.
     * 
     * @param email user email
     * @return true if user exists
     */
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if user exists by username.
     * 
     * @param username username
     * @return true if user exists
     */
    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Get user by email.
     * 
     * @param email user email
     * @return user entity if found
     * @throws AuthenticationException if user not found
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found with email: " + email));
    }

    /**
     * Get user by ID.
     * 
     * @param userId user ID
     * @return user entity if found
     * @throws AuthenticationException if user not found
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found with ID: " + userId));
    }

    /**
     * Find user by username.
     * 
     * @param username username
     * @return optional user entity
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email.
     * 
     * @param email user email
     * @return optional user entity
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Validate password requirements beyond basic annotations.
     * Requirements: 8+ chars, uppercase, lowercase, numeric
     * 
     * @param password password to validate
     * @throws IllegalArgumentException if password doesn't meet requirements
     */
    private void validatePasswordRequirements(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUppercase) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (!hasLowercase) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        if (!hasDigit) {
            throw new IllegalArgumentException("Password must contain at least one numeric character");
        }

        logger.debug("Password validation passed for user registration");
    }
}