package com.swnih.controller;

import com.swnih.dto.OAuthAuthorizationRequest;
import com.swnih.dto.OAuthCallbackRequest;
import com.swnih.dto.OAuthTokenResponse;
import com.swnih.entity.User;
import com.swnih.exception.OAuthException;
import com.swnih.service.GmailIntegrationService;
import com.swnih.service.AuthenticationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Gmail OAuth integration endpoints.
 * Handles OAuth flow initiation, callback processing, and authorization management.
 */
@RestController
@RequestMapping("/api/gmail")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class GmailController {

    private static final Logger logger = LoggerFactory.getLogger(GmailController.class);

    private final GmailIntegrationService gmailIntegrationService;
    private final AuthenticationService authenticationService;

    public GmailController(GmailIntegrationService gmailIntegrationService,
                          AuthenticationService authenticationService) {
        this.gmailIntegrationService = gmailIntegrationService;
        this.authenticationService = authenticationService;
    }

    /**
     * Initiate Gmail OAuth 2.0 authorization flow.
     * 
     * @return OAuthAuthorizationRequest containing authorization URL
     */
    @GetMapping("/connect")
    public ResponseEntity<?> initiateGmailConnection() {
        try {
            logger.info("Gmail connect endpoint called");

            // Get current authenticated user
            User currentUser = getCurrentUser();
            logger.info("Current user: {}", currentUser.getUsername());
            
            // Check if already connected
            if (gmailIntegrationService.hasValidAuthorization(currentUser)) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Gmail is already connected!",
                    "alreadyConnected", true
                ));
            }
            
            // Build proper OAuth URL directly
            String clientId = "549183291633-613jveqvl64qh55fta2ttl0uft94bggg.apps.googleusercontent.com";
            String redirectUri = "http://localhost:8080/api/gmail/oauth/callback";
            String scope = "https://www.googleapis.com/auth/gmail.readonly";
            String state = java.util.UUID.randomUUID().toString();
            
            // Store state for this user (simple approach)
            // In production, you'd use a more robust state management
            
            String authUrl = "https://accounts.google.com/o/oauth2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + java.net.URLEncoder.encode(redirectUri, "UTF-8") +
                "&scope=" + java.net.URLEncoder.encode(scope, "UTF-8") +
                "&response_type=code" +
                "&access_type=offline" +
                "&approval_prompt=force" +
                "&state=" + state;
            
            logger.info("Generated OAuth URL successfully");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Gmail OAuth is configured and ready!",
                "authorizationUrl", authUrl,
                "state", state
            ));

        } catch (Exception e) {
            logger.error("Error in Gmail connect endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "Error: " + e.getMessage()
                    ));
        }
    }

    /**
     * Handle OAuth callback from Google.
     * This endpoint receives the authorization code and exchanges it for tokens.
     * 
     * @param code the authorization code from Google
     * @param state the state parameter for CSRF protection
     * @param error optional error parameter from OAuth provider
     * @param errorDescription optional error description from OAuth provider
     * @return redirect to dashboard with success/error status
     */
    @GetMapping("/oauth/callback")
    public RedirectView handleOAuthCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription) {
        
        try {
            logger.info("Handling OAuth callback with code: {}, state: {}, error: {}", 
                       code != null ? "present" : "null", state, error);

            // Check for OAuth errors first
            if (error != null) {
                logger.error("OAuth error in callback: {} - {}", error, errorDescription);
                String errorUrl = "/dashboard?gmail_error=" + java.net.URLEncoder.encode(error, "UTF-8");
                return new RedirectView(errorUrl);
            }

            // Check if we have an authorization code
            if (code == null || code.trim().isEmpty()) {
                logger.error("No authorization code received in OAuth callback");
                return new RedirectView("/dashboard?gmail_error=no_code");
            }

            // Store the code temporarily and redirect to dashboard with success
            // The dashboard will handle the code exchange
            logger.info("Received authorization code, redirecting to dashboard with success");
            
            String successUrl = "/dashboard?gmail_code=" + java.net.URLEncoder.encode(code, "UTF-8") + 
                               "&gmail_state=" + java.net.URLEncoder.encode(state != null ? state : "", "UTF-8");
            
            return new RedirectView(successUrl);

        } catch (Exception e) {
            logger.error("Unexpected error handling OAuth callback", e);
            try {
                String errorUrl = "/dashboard?gmail_error=" + 
                                java.net.URLEncoder.encode("OAuth callback processing failed", "UTF-8");
                return new RedirectView(errorUrl);
            } catch (Exception encodingError) {
                return new RedirectView("/dashboard?gmail_error=callback_failed");
            }
        }
    }

    /**
     * Process OAuth callback via POST (alternative endpoint for frontend integration).
     * 
     * @param callbackRequest the OAuth callback request
     * @return OAuthTokenResponse indicating success or failure
     */
    @PostMapping("/oauth/callback")
    public ResponseEntity<?> processOAuthCallback(@Valid @RequestBody OAuthCallbackRequest callbackRequest) {
        try {
            logger.info("Processing OAuth callback via POST with state: {}", callbackRequest.getState());

            User currentUser = getCurrentUser();
            OAuthTokenResponse response = gmailIntegrationService.handleOAuthCallbackForUser(callbackRequest, currentUser);

            if (response.isSuccess()) {
                logger.info("OAuth callback processing successful");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", response.getMessage(),
                    "expiresAt", response.getExpiresAt(),
                    "createdAt", response.getCreatedAt()
                ));
            } else {
                logger.warn("OAuth callback processing failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                            "success", false,
                            "message", response.getMessage()
                        ));
            }

        } catch (OAuthException e) {
            logger.error("OAuth error during callback processing", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "error", e.getErrorCode(),
                        "message", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Unexpected error during callback processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "An unexpected error occurred"
                    ));
        }
    }

    /**
     * Exchange authorization code for tokens (called from frontend after OAuth callback).
     * 
     * @param request containing the authorization code
     * @return success/failure response
     */
    @PostMapping("/exchange-code")
    public ResponseEntity<?> exchangeAuthorizationCode(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            String state = request.get("state");
            
            logger.info("Exchanging authorization code for tokens");

            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                            "success", false,
                            "message", "Authorization code is required"
                        ));
            }

            // Get current authenticated user
            User currentUser = getCurrentUser();
            logger.info("Exchanging code for user: {}", currentUser.getUsername());

            // Create callback request
            OAuthCallbackRequest callbackRequest = new OAuthCallbackRequest();
            callbackRequest.setCode(code);
            callbackRequest.setState(state);

            // Process the callback
            OAuthTokenResponse response = gmailIntegrationService.handleOAuthCallbackForUser(callbackRequest, currentUser);

            if (response.isSuccess()) {
                logger.info("Successfully exchanged authorization code for tokens");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Gmail connected successfully!",
                    "expiresAt", response.getExpiresAt(),
                    "createdAt", response.getCreatedAt()
                ));
            } else {
                logger.warn("Failed to exchange authorization code: {}", response.getMessage());
                return ResponseEntity.badRequest()
                        .body(Map.of(
                            "success", false,
                            "message", response.getMessage()
                        ));
            }

        } catch (Exception e) {
            logger.error("Error exchanging authorization code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "Failed to exchange authorization code: " + e.getMessage()
                    ));
        }
    }
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.info("Authentication object: {}", authentication);
            logger.info("Authentication name: {}", authentication != null ? authentication.getName() : "null");
            logger.info("Authentication principal: {}", authentication != null ? authentication.getPrincipal() : "null");
            logger.info("Is authenticated: {}", authentication != null ? authentication.isAuthenticated() : "false");
            
            User currentUser = getCurrentUser();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Authentication working",
                "userId", currentUser.getId(),
                "username", currentUser.getUsername(),
                "email", currentUser.getEmail(),
                "authName", authentication.getName()
            ));
        } catch (Exception e) {
            logger.error("Test endpoint error", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "error", "AUTH_ERROR",
                        "message", e.getMessage()
                    ));
        }
    }

    /**
     * Check Gmail connection status for the current user.
     * 
     * @return connection status information
     */
    @GetMapping("/status")
    public ResponseEntity<?> getConnectionStatus() {
        try {
            User currentUser = getCurrentUser();
            boolean isConnected = gmailIntegrationService.hasValidAuthorization(currentUser);

            logger.debug("Gmail connection status for user {}: {}", currentUser.getId(), isConnected);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "connected", isConnected,
                "message", isConnected ? "Gmail is connected" : "Gmail is not connected"
            ));

        } catch (Exception e) {
            logger.error("Error checking Gmail connection status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "Failed to check connection status"
                    ));
        }
    }

    /**
     * Disconnect Gmail integration for the current user.
     * 
     * @return disconnection result
     */
    @DeleteMapping("/disconnect")
    public ResponseEntity<?> disconnectGmail() {
        try {
            User currentUser = getCurrentUser();
            
            logger.info("Disconnecting Gmail for user: {}", currentUser.getId());
            gmailIntegrationService.revokeAuthorization(currentUser);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Gmail has been disconnected successfully"
            ));

        } catch (OAuthException e) {
            logger.error("OAuth error during Gmail disconnection", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "error", e.getErrorCode(),
                        "message", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Unexpected error during Gmail disconnection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "An unexpected error occurred"
                    ));
        }
    }

    /**
     * Refresh OAuth tokens for the current user.
     * 
     * @return refresh result
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken() {
        try {
            User currentUser = getCurrentUser();
            
            logger.info("Refreshing OAuth token for user: {}", currentUser.getId());
            
            // This will automatically refresh the token if needed
            String accessToken = gmailIntegrationService.getValidAccessToken(currentUser);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token refreshed successfully"
            ));

        } catch (OAuthException e) {
            logger.error("OAuth error during token refresh", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "error", e.getErrorCode(),
                        "message", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Unexpected error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "An unexpected error occurred"
                    ));
        }
    }

    /**
     * Fetch messages from Gmail for the current user.
     * 
     * @return list of processed messages with priority classification
     */
    @GetMapping("/messages")
    public ResponseEntity<?> fetchGmailMessages() {
        try {
            User currentUser = getCurrentUser();
            
            logger.info("Fetching Gmail messages for user: {}", currentUser.getId());
            
            // Check if user has valid authorization
            if (!gmailIntegrationService.hasValidAuthorization(currentUser)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                            "success", false,
                            "error", "GMAIL_NOT_CONNECTED",
                            "message", "Gmail is not connected. Please connect Gmail first."
                        ));
            }
            
            // Fetch and process messages
            var messages = gmailIntegrationService.fetchAndProcessMessages(currentUser);
            
            logger.info("Successfully fetched {} messages for user: {}", messages.size(), currentUser.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Messages fetched successfully",
                "messages", messages,
                "count", messages.size()
            ));

        } catch (OAuthException e) {
            logger.error("OAuth error during message fetching", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "error", e.getErrorCode(),
                        "message", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Unexpected error during message fetching", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "INTERNAL_ERROR",
                        "message", "Failed to fetch messages"
                    ));
        }
    }

    /**
     * Get the current authenticated user.
     * 
     * @return current user
     * @throws RuntimeException if no user is authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("No authentication found in security context");
            throw new RuntimeException("No authenticated user found");
        }

        // Check if authentication is anonymous
        if ("anonymousUser".equals(authentication.getName())) {
            logger.error("Anonymous user found in security context");
            throw new RuntimeException("No authenticated user found");
        }

        String email = authentication.getName();
        logger.debug("Looking for user with email: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            logger.error("Email is null or empty from authentication: {}", email);
            throw new RuntimeException("Invalid authentication - no email found");
        }
        
        Optional<User> userOptional = authenticationService.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.error("User not found in database with email: {}", email);
            // Let's also try to find by username in case there's a mismatch
            logger.debug("Attempting to find user by username: {}", email);
            userOptional = authenticationService.findByUsername(email);
        }
        
        return userOptional.orElseThrow(() -> new RuntimeException("Authenticated user not found in database: " + email));
    }
}