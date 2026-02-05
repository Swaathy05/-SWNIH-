package com.swnih.service;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.swnih.dto.OAuthAuthorizationRequest;
import com.swnih.dto.OAuthCallbackRequest;
import com.swnih.dto.OAuthTokenResponse;
import com.swnih.entity.OAuthToken;
import com.swnih.entity.User;
import com.swnih.exception.OAuthException;
import com.swnih.exception.TokenRefreshException;
import com.swnih.repository.OAuthTokenRepository;
import com.swnih.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling Gmail OAuth 2.0 integration.
 * Manages OAuth flow initiation, callback handling, token storage, and automatic refresh.
 */
@Service
@Transactional
public class GmailIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(GmailIntegrationService.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Smart Web Notification Intelligence Hub";
    
    // Store state parameters temporarily (in production, use Redis or database)
    private final ConcurrentHashMap<String, Long> stateToUserIdMap = new ConcurrentHashMap<>();

    private final OAuthTokenRepository oauthTokenRepository;
    private final EncryptionService encryptionService;
    private final NetHttpTransport httpTransport;
    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${gmail.api.max-messages:50}")
    private int maxMessages;

    @Value("${gmail.api.rate-limit.requests-per-second:10}")
    private int requestsPerSecond;

    @Value("${gmail.api.rate-limit.max-retries:3}")
    private int maxRetries;

    @Value("${gmail.api.rate-limit.backoff-multiplier:2}")
    private int backoffMultiplier;

    public GmailIntegrationService(OAuthTokenRepository oauthTokenRepository, 
                                 EncryptionService encryptionService,
                                 UserRepository userRepository) throws GeneralSecurityException, IOException {
        this.oauthTokenRepository = oauthTokenRepository;
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    /**
     * Initiate Gmail OAuth 2.0 authorization flow.
     * 
     * @param user the user requesting Gmail integration
     * @return OAuthAuthorizationRequest containing authorization URL and state
     * @throws OAuthException if OAuth flow initiation fails
     */
    public OAuthAuthorizationRequest initiateOAuthFlow(User user) {
        try {
            if (user == null) {
                throw new OAuthException("User cannot be null", "INVALID_USER");
            }
            
            logger.info("Initiating OAuth flow for user: {}", user.getId());

            // Generate state parameter for CSRF protection
            String state = generateStateParameter();
            stateToUserIdMap.put(state, user.getId());

            // Build authorization URL manually (simpler approach)
            String authorizationUrl = "https://accounts.google.com/o/oauth2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + java.net.URLEncoder.encode(redirectUri, "UTF-8") +
                "&scope=" + java.net.URLEncoder.encode("https://www.googleapis.com/auth/gmail.readonly", "UTF-8") +
                "&response_type=code" +
                "&access_type=offline" +
                "&approval_prompt=force" +
                "&state=" + state;

            logger.info("Generated OAuth authorization URL for user: {}", user.getId());
            return new OAuthAuthorizationRequest(authorizationUrl, state);

        } catch (Exception e) {
            logger.error("Failed to initiate OAuth flow for user: {}", user != null ? user.getId() : "null", e);
            throw new OAuthException("Failed to initiate Gmail OAuth flow: " + e.getMessage(), "OAUTH_INITIATION_FAILED", e);
        }
    }

    /**
     * Handle OAuth callback for a specific user (bypassing state validation).
     * 
     * @param callbackRequest the OAuth callback request
     * @param user the user to associate the tokens with
     * @return OAuthTokenResponse indicating success or failure
     * @throws OAuthException if callback handling fails
     */
    public OAuthTokenResponse handleOAuthCallbackForUser(OAuthCallbackRequest callbackRequest, User user) {
        try {
            logger.info("Handling OAuth callback for user: {}", user.getId());

            // Check for OAuth errors
            if (callbackRequest.hasError()) {
                logger.error("OAuth callback contains error: {} - {}", 
                           callbackRequest.getError(), callbackRequest.getErrorDescription());
                return OAuthTokenResponse.failure("OAuth authorization failed: " + callbackRequest.getError());
            }

            // Exchange authorization code for tokens
            GoogleTokenResponse tokenResponse = exchangeCodeForTokens(callbackRequest.getCode());

            // Store encrypted tokens for the specific user
            OAuthToken oauthToken = storeTokensForUser(user, tokenResponse);

            logger.info("Successfully stored OAuth tokens for user: {}", user.getId());
            return OAuthTokenResponse.success(
                "Gmail integration successful", 
                oauthToken.getExpiresAt(), 
                oauthToken.getCreatedAt()
            );

        } catch (Exception e) {
            logger.error("Failed to handle OAuth callback for user: {}", user.getId(), e);
            throw new OAuthException("Failed to handle OAuth callback", "OAUTH_CALLBACK_FAILED", e);
        }
    }

    /**
     * Get valid access token for a user, refreshing if necessary.
     * 
     * @param user the user to get token for
     * @return decrypted access token
     * @throws OAuthException if no valid token exists or refresh fails
     */
    public String getValidAccessToken(User user) {
        try {
            logger.debug("Getting valid access token for user: {}", user.getId());

            Optional<OAuthToken> tokenOpt = oauthTokenRepository.findValidTokenByUser(user, LocalDateTime.now());
            
            if (tokenOpt.isEmpty()) {
                logger.warn("No valid OAuth token found for user: {}", user.getId());
                throw new OAuthException("No valid Gmail authorization found. Please reconnect your Gmail account.", "NO_VALID_TOKEN");
            }

            OAuthToken token = tokenOpt.get();

            // Check if token is expiring soon (within 5 minutes) and refresh if needed
            if (token.isExpiringSoon(5)) {
                logger.info("Token expiring soon for user: {}, attempting refresh", user.getId());
                token = refreshAccessToken(token);
            }

            return encryptionService.decrypt(token.getAccessTokenEncrypted());

        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get valid access token for user: {}", user.getId(), e);
            throw new OAuthException("Failed to get valid access token", "TOKEN_RETRIEVAL_FAILED", e);
        }
    }

    /**
     * Refresh an expired or expiring access token using the refresh token.
     * 
     * @param token the OAuth token to refresh
     * @return updated OAuth token with new access token
     * @throws TokenRefreshException if token refresh fails
     */
    public OAuthToken refreshAccessToken(OAuthToken token) {
        try {
            logger.info("Refreshing access token for user: {}", token.getUser().getId());

            String refreshToken = encryptionService.decrypt(token.getRefreshTokenEncrypted());

            // Create Google client secrets
            GoogleClientSecrets clientSecrets = createClientSecrets();

            // Build the authorization flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, Collections.singletonList(GmailScopes.GMAIL_READONLY))
                    .setDataStoreFactory(new MemoryDataStoreFactory())
                    .build();

            // Refresh the token
            GoogleTokenResponse tokenResponse = flow.newTokenRequest(refreshToken)
                    .setRedirectUri(redirectUri)
                    .execute();

            // Update the stored token
            String newAccessToken = encryptionService.encrypt(tokenResponse.getAccessToken());
            LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds());

            token.setAccessTokenEncrypted(newAccessToken);
            token.setExpiresAt(newExpiresAt);

            // Update refresh token if provided (Google may rotate refresh tokens)
            if (tokenResponse.getRefreshToken() != null) {
                String newRefreshToken = encryptionService.encrypt(tokenResponse.getRefreshToken());
                token.setRefreshTokenEncrypted(newRefreshToken);
            }

            OAuthToken savedToken = oauthTokenRepository.save(token);
            logger.info("Successfully refreshed access token for user: {}", token.getUser().getId());

            return savedToken;

        } catch (Exception e) {
            logger.error("Failed to refresh access token for user: {}", token.getUser().getId(), e);
            throw new TokenRefreshException("Failed to refresh access token", "TOKEN_REFRESH_FAILED", e);
        }
    }

    /**
     * Create Gmail API client with valid credentials for a user.
     * 
     * @param user the user to create Gmail client for
     * @return configured Gmail API client
     * @throws OAuthException if Gmail client creation fails
     */
    public Gmail createGmailClient(User user) {
        try {
            String accessToken = getValidAccessToken(user);

            // Create credential with access token
            Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setTokenServerUrl(new com.google.api.client.http.GenericUrl("https://oauth2.googleapis.com/token"))
                    .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
                    .build();

            credential.setAccessToken(accessToken);

            return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (Exception e) {
            logger.error("Failed to create Gmail client for user: {}", user.getId(), e);
            throw new OAuthException("Failed to create Gmail client", "GMAIL_CLIENT_CREATION_FAILED", e);
        }
    }

    /**
     * Check if user has valid Gmail authorization.
     * 
     * @param user the user to check
     * @return true if user has valid Gmail authorization
     */
    public boolean hasValidAuthorization(User user) {
        try {
            return oauthTokenRepository.hasValidToken(user, LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Failed to check authorization status for user: {}", user.getId(), e);
            return false;
        }
    }

    /**
     * Revoke Gmail authorization for a user.
     * 
     * @param user the user to revoke authorization for
     */
    public void revokeAuthorization(User user) {
        try {
            logger.info("Revoking Gmail authorization for user: {}", user.getId());

            // Delete all OAuth tokens for the user
            long deletedCount = oauthTokenRepository.deleteByUser(user);
            logger.info("Deleted {} OAuth tokens for user: {}", deletedCount, user.getId());

        } catch (Exception e) {
            logger.error("Failed to revoke authorization for user: {}", user.getId(), e);
            throw new OAuthException("Failed to revoke Gmail authorization", "AUTHORIZATION_REVOCATION_FAILED", e);
        }
    }

    // Private helper methods

    private GoogleClientSecrets createClientSecrets() {
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setInstalled(details);

        return clientSecrets;
    }

    private String generateStateParameter() {
        return UUID.randomUUID().toString();
    }

    /**
     * Fetch and process messages from Gmail for a user.
     * 
     * @param user the user to fetch messages for
     * @return list of processed messages with priority classification
     * @throws OAuthException if message fetching fails
     */
    public java.util.List<java.util.Map<String, Object>> fetchAndProcessMessages(User user) {
        try {
            logger.info("Fetching Gmail messages for user: {}", user.getId());

            Gmail gmail = createGmailClient(user);
            
            // List messages from inbox
            com.google.api.services.gmail.model.ListMessagesResponse response = gmail.users().messages()
                    .list("me")
                    .setMaxResults((long) maxMessages)
                    .setQ("in:inbox")
                    .execute();

            java.util.List<java.util.Map<String, Object>> processedMessages = new java.util.ArrayList<>();

            if (response.getMessages() != null) {
                logger.info("Found {} messages in Gmail inbox for user: {}", response.getMessages().size(), user.getId());

                for (com.google.api.services.gmail.model.Message message : response.getMessages()) {
                    try {
                        // Get full message details
                        com.google.api.services.gmail.model.Message fullMessage = gmail.users().messages()
                                .get("me", message.getId())
                                .execute();

                        // Process and classify the message
                        java.util.Map<String, Object> processedMessage = processMessage(fullMessage);
                        if (processedMessage != null) {
                            processedMessages.add(processedMessage);
                        }

                        // Rate limiting - simple delay
                        Thread.sleep(100); // 10 requests per second max

                    } catch (Exception e) {
                        logger.warn("Failed to process message {}: {}", message.getId(), e.getMessage());
                        // Continue with other messages
                    }
                }
            }

            logger.info("Successfully processed {} messages for user: {}", processedMessages.size(), user.getId());
            return processedMessages;

        } catch (Exception e) {
            logger.error("Failed to fetch Gmail messages for user: {}", user.getId(), e);
            throw new OAuthException("Failed to fetch Gmail messages", "MESSAGE_FETCH_FAILED", e);
        }
    }

    /**
     * Process a Gmail message and classify its priority.
     * 
     * @param message the Gmail message to process
     * @return processed message map with priority classification
     */
    private java.util.Map<String, Object> processMessage(com.google.api.services.gmail.model.Message message) {
        try {
            java.util.Map<String, Object> processedMessage = new java.util.HashMap<>();

            // Extract message headers
            String sender = "";
            String subject = "";
            String date = "";

            if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
                for (com.google.api.services.gmail.model.MessagePartHeader header : message.getPayload().getHeaders()) {
                    switch (header.getName().toLowerCase()) {
                        case "from":
                            sender = header.getValue();
                            break;
                        case "subject":
                            subject = header.getValue();
                            break;
                        case "date":
                            date = header.getValue();
                            break;
                    }
                }
            }

            // Extract message body (simplified)
            String body = extractMessageBody(message.getPayload());

            // Classify priority using keyword-based classification
            String priority = classifyMessagePriority(subject, body, sender);

            // Build processed message
            processedMessage.put("id", message.getId());
            processedMessage.put("sender", cleanEmailAddress(sender));
            processedMessage.put("subject", subject != null ? subject : "No Subject");
            processedMessage.put("body", body != null ? body.substring(0, Math.min(body.length(), 200)) + "..." : "");
            processedMessage.put("priority", priority);
            processedMessage.put("timestamp", parseDate(date));
            processedMessage.put("source", "GMAIL");

            return processedMessage;

        } catch (Exception e) {
            logger.warn("Failed to process message: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract message body from Gmail message payload.
     * 
     * @param payload the message payload
     * @return extracted body text
     */
    private String extractMessageBody(com.google.api.services.gmail.model.MessagePart payload) {
        if (payload == null) return "";

        StringBuilder body = new StringBuilder();

        if (payload.getBody() != null && payload.getBody().getData() != null) {
            byte[] data = com.google.api.client.util.Base64.decodeBase64(payload.getBody().getData());
            body.append(new String(data, java.nio.charset.StandardCharsets.UTF_8));
        }

        if (payload.getParts() != null) {
            for (com.google.api.services.gmail.model.MessagePart part : payload.getParts()) {
                if ("text/plain".equals(part.getMimeType()) && part.getBody() != null && part.getBody().getData() != null) {
                    byte[] data = com.google.api.client.util.Base64.decodeBase64(part.getBody().getData());
                    body.append(new String(data, java.nio.charset.StandardCharsets.UTF_8));
                    break; // Use first text/plain part
                }
            }
        }

        return body.toString().trim();
    }

    /**
     * Classify message priority based on keywords and content.
     * 
     * @param subject message subject
     * @param body message body
     * @param sender message sender
     * @return priority level (HIGH, MEDIUM, LOW)
     */
    private String classifyMessagePriority(String subject, String body, String sender) {
        String content = ((subject != null ? subject : "") + " " + (body != null ? body : "")).toLowerCase();

        // High priority keywords
        String[] highKeywords = {"interview", "offer", "urgent", "deadline", "exam", "emergency", "important", "asap", "critical"};
        for (String keyword : highKeywords) {
            if (content.contains(keyword)) {
                return "HIGH";
            }
        }

        // Medium priority keywords
        String[] mediumKeywords = {"meeting", "reminder", "schedule", "appointment", "update", "notification", "alert"};
        for (String keyword : mediumKeywords) {
            if (content.contains(keyword)) {
                return "MEDIUM";
            }
        }

        // Low priority keywords (promotional/marketing)
        String[] lowKeywords = {"sale", "discount", "offer", "promotion", "newsletter", "unsubscribe", "marketing"};
        for (String keyword : lowKeywords) {
            if (content.contains(keyword)) {
                return "LOW";
            }
        }

        // Default to MEDIUM if no keywords match
        return "MEDIUM";
    }

    /**
     * Clean email address to extract just the email part.
     * 
     * @param emailString full email string (e.g., "John Doe <john@example.com>")
     * @return cleaned email address
     */
    private String cleanEmailAddress(String emailString) {
        if (emailString == null) return "";
        
        // Extract email from "Name <email@domain.com>" format
        int start = emailString.indexOf('<');
        int end = emailString.indexOf('>');
        
        if (start != -1 && end != -1 && end > start) {
            return emailString.substring(start + 1, end);
        }
        
        return emailString;
    }

    /**
     * Parse date string to LocalDateTime.
     * 
     * @param dateString date string from email header
     * @return parsed LocalDateTime or current time if parsing fails
     */
    private java.time.LocalDateTime parseDate(String dateString) {
        try {
            if (dateString == null || dateString.isEmpty()) {
                return java.time.LocalDateTime.now();
            }
            
            // Simple date parsing - in production, use proper RFC 2822 parser
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
            return java.time.LocalDateTime.parse(dateString, formatter);
        } catch (Exception e) {
            logger.debug("Failed to parse date: {}", dateString);
            return java.time.LocalDateTime.now();
        }
    }

    private GoogleTokenResponse exchangeCodeForTokens(String authorizationCode) throws IOException {
        GoogleClientSecrets clientSecrets = createClientSecrets();

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, Collections.singletonList(GmailScopes.GMAIL_READONLY))
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline")
                .build();

        return flow.newTokenRequest(authorizationCode)
                .setRedirectUri(redirectUri)
                .execute();
    }

    private OAuthToken storeTokensForUser(User user, GoogleTokenResponse tokenResponse) {
        // Encrypt tokens
        String encryptedAccessToken = encryptionService.encrypt(tokenResponse.getAccessToken());
        String encryptedRefreshToken = encryptionService.encrypt(tokenResponse.getRefreshToken());

        // Calculate expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds());

        // Create and save OAuth token
        OAuthToken oauthToken = new OAuthToken(user, encryptedAccessToken, encryptedRefreshToken, expiresAt);
        return oauthTokenRepository.save(oauthToken);
    }

    private OAuthToken storeTokens(Long userId, GoogleTokenResponse tokenResponse) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OAuthException("User not found with ID: " + userId, "USER_NOT_FOUND"));

        return storeTokensForUser(user, tokenResponse);
    }
}