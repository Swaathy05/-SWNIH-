package com.swnih.repository;

import com.swnih.entity.OAuthToken;
import com.swnih.entity.User;
import com.swnih.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for OAuthTokenRepository.
 * Tests repository methods with actual database operations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("OAuthTokenRepository Tests")
class OAuthTokenRepositoryTest {

    @Autowired
    private OAuthTokenRepository oauthTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;
    private OAuthToken validToken;
    private OAuthToken expiredToken;
    private OAuthToken expiringSoonToken;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        oauthTokenRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test users
        testUser = new User("testuser", "test@example.com", "hashedPassword");
        anotherUser = new User("anotheruser", "another@example.com", "hashedPassword");
        
        testUser = userRepository.save(testUser);
        anotherUser = userRepository.save(anotherUser);

        // Create test tokens
        LocalDateTime now = LocalDateTime.now();
        
        validToken = new OAuthToken(
            testUser,
            "encryptedAccessToken1",
            "encryptedRefreshToken1",
            now.plusHours(2)
        );
        
        expiredToken = new OAuthToken(
            testUser,
            "encryptedAccessToken2",
            "encryptedRefreshToken2",
            now.minusHours(1)
        );
        
        expiringSoonToken = new OAuthToken(
            testUser,
            "encryptedAccessToken3",
            "encryptedRefreshToken3",
            now.plusMinutes(5)
        );

        validToken = oauthTokenRepository.save(validToken);
        expiredToken = oauthTokenRepository.save(expiredToken);
        expiringSoonToken = oauthTokenRepository.save(expiringSoonToken);
    }

    @Test
    @DisplayName("Should find most recent token for user")
    void shouldFindMostRecentTokenForUser() {
        Optional<OAuthToken> result = oauthTokenRepository.findTopByUserOrderByCreatedAtDesc(testUser);
        
        assertThat(result).isPresent();
        // The most recent should be expiringSoonToken as it was created last
        assertThat(result.get().getAccessTokenEncrypted()).isEqualTo("encryptedAccessToken3");
    }

    @Test
    @DisplayName("Should return empty when no tokens exist for user")
    void shouldReturnEmptyWhenNoTokensExistForUser() {
        Optional<OAuthToken> result = oauthTokenRepository.findTopByUserOrderByCreatedAtDesc(anotherUser);
        
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all tokens for user ordered by creation date")
    void shouldFindAllTokensForUserOrderedByCreationDate() {
        List<OAuthToken> tokens = oauthTokenRepository.findByUserOrderByCreatedAtDesc(testUser);
        
        assertThat(tokens).hasSize(3);
        // Should be ordered by creation date descending
        assertThat(tokens.get(0).getAccessTokenEncrypted()).isEqualTo("encryptedAccessToken3");
        assertThat(tokens.get(1).getAccessTokenEncrypted()).isEqualTo("encryptedAccessToken2");
        assertThat(tokens.get(2).getAccessTokenEncrypted()).isEqualTo("encryptedAccessToken1");
    }

    @Test
    @DisplayName("Should find valid token for user")
    void shouldFindValidTokenForUser() {
        LocalDateTime now = LocalDateTime.now();
        Optional<OAuthToken> result = oauthTokenRepository.findValidTokenByUser(testUser, now);
        
        assertThat(result).isPresent();
        // Should return the most recent valid token (not expired)
        assertThat(result.get().getExpiresAt()).isAfter(now);
    }

    @Test
    @DisplayName("Should not find valid token when all are expired")
    void shouldNotFindValidTokenWhenAllAreExpired() {
        // Create a user with only expired tokens
        User userWithExpiredTokens = new User("expireduser", "expired@example.com", "hashedPassword");
        userWithExpiredTokens = userRepository.save(userWithExpiredTokens);
        
        OAuthToken expiredToken1 = new OAuthToken(
            userWithExpiredTokens,
            "expiredToken1",
            "expiredRefresh1",
            LocalDateTime.now().minusHours(2)
        );
        oauthTokenRepository.save(expiredToken1);
        
        LocalDateTime now = LocalDateTime.now();
        Optional<OAuthToken> result = oauthTokenRepository.findValidTokenByUser(userWithExpiredTokens, now);
        
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find tokens expiring soon")
    void shouldFindTokensExpiringSoon() {
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(10);
        List<OAuthToken> tokens = oauthTokenRepository.findTokensExpiringSoon(threshold);
        
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getAccessTokenEncrypted()).isEqualTo("encryptedAccessToken3");
    }

    @Test
    @DisplayName("Should find expired tokens")
    void shouldFindExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        List<OAuthToken> tokens = oauthTokenRepository.findExpiredTokens(now);
        
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getAccessTokenEncrypted()).isEqualTo("encryptedAccessToken2");
    }

    @Test
    @DisplayName("Should delete expired tokens")
    void shouldDeleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        int deletedCount = oauthTokenRepository.deleteExpiredTokens(now);
        
        assertThat(deletedCount).isEqualTo(1);
        
        // Verify the expired token is deleted
        List<OAuthToken> remainingTokens = oauthTokenRepository.findByUserOrderByCreatedAtDesc(testUser);
        assertThat(remainingTokens).hasSize(2);
        assertThat(remainingTokens).noneMatch(token -> 
            token.getAccessTokenEncrypted().equals("encryptedAccessToken2"));
    }

    @Test
    @DisplayName("Should delete all tokens for user")
    void shouldDeleteAllTokensForUser() {
        long deletedCount = oauthTokenRepository.deleteByUser(testUser);
        
        assertThat(deletedCount).isEqualTo(3);
        
        // Verify all tokens are deleted
        List<OAuthToken> remainingTokens = oauthTokenRepository.findByUserOrderByCreatedAtDesc(testUser);
        assertThat(remainingTokens).isEmpty();
    }

    @Test
    @DisplayName("Should check if user has valid token")
    void shouldCheckIfUserHasValidToken() {
        LocalDateTime now = LocalDateTime.now();
        
        boolean hasValidToken = oauthTokenRepository.hasValidToken(testUser, now);
        boolean anotherUserHasValidToken = oauthTokenRepository.hasValidToken(anotherUser, now);
        
        assertThat(hasValidToken).isTrue();
        assertThat(anotherUserHasValidToken).isFalse();
    }

    @Test
    @DisplayName("Should count tokens for user")
    void shouldCountTokensForUser() {
        long tokenCount = oauthTokenRepository.countByUser(testUser);
        long anotherUserTokenCount = oauthTokenRepository.countByUser(anotherUser);
        
        assertThat(tokenCount).isEqualTo(3);
        assertThat(anotherUserTokenCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should find tokens created after specific date")
    void shouldFindTokensCreatedAfterSpecificDate() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(30);
        List<OAuthToken> tokens = oauthTokenRepository.findTokensCreatedAfter(testUser, since);
        
        // All tokens should be found as they were created recently
        assertThat(tokens).hasSize(3);
    }

    @Test
    @DisplayName("Should update token expiration")
    void shouldUpdateTokenExpiration() {
        LocalDateTime newExpiration = LocalDateTime.now().plusDays(1);
        
        int updatedCount = oauthTokenRepository.updateTokenExpiration(validToken.getId(), newExpiration);
        
        assertThat(updatedCount).isEqualTo(1);
        
        // Verify the expiration was updated
        OAuthToken updatedToken = oauthTokenRepository.findById(validToken.getId()).orElse(null);
        assertThat(updatedToken).isNotNull();
        assertThat(updatedToken.getExpiresAt()).isEqualToIgnoringNanos(newExpiration);
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void shouldHandleEmptyResultsGracefully() {
        User newUser = new User("newuser", "new@example.com", "hashedPassword");
        newUser = userRepository.save(newUser);
        
        Optional<OAuthToken> mostRecent = oauthTokenRepository.findTopByUserOrderByCreatedAtDesc(newUser);
        List<OAuthToken> allTokens = oauthTokenRepository.findByUserOrderByCreatedAtDesc(newUser);
        Optional<OAuthToken> validToken = oauthTokenRepository.findValidTokenByUser(newUser, LocalDateTime.now());
        boolean hasValidToken = oauthTokenRepository.hasValidToken(newUser, LocalDateTime.now());
        long tokenCount = oauthTokenRepository.countByUser(newUser);
        
        assertThat(mostRecent).isEmpty();
        assertThat(allTokens).isEmpty();
        assertThat(validToken).isEmpty();
        assertThat(hasValidToken).isFalse();
        assertThat(tokenCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should maintain referential integrity with user")
    void shouldMaintainReferentialIntegrityWithUser() {
        // Create a token
        OAuthToken newToken = new OAuthToken(
            testUser,
            "newAccessToken",
            "newRefreshToken",
            LocalDateTime.now().plusHours(1)
        );
        
        OAuthToken savedToken = oauthTokenRepository.save(newToken);
        
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getUser()).isEqualTo(testUser);
        
        // Verify the relationship is maintained
        OAuthToken foundToken = oauthTokenRepository.findById(savedToken.getId()).orElse(null);
        assertThat(foundToken).isNotNull();
        assertThat(foundToken.getUser().getId()).isEqualTo(testUser.getId());
    }
}