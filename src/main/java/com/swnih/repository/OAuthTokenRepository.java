package com.swnih.repository;

import com.swnih.entity.OAuthToken;
import com.swnih.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for OAuthToken entity operations.
 * Provides CRUD operations and custom queries for OAuth token management.
 */
@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {

    /**
     * Find the most recent OAuth token for a user.
     * @param user the user to find token for
     * @return Optional containing the most recent token if found
     */
    Optional<OAuthToken> findTopByUserOrderByCreatedAtDesc(User user);

    /**
     * Find all OAuth tokens for a user.
     * @param user the user to find tokens for
     * @return list of OAuth tokens for the user
     */
    List<OAuthToken> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find valid (non-expired) OAuth token for a user.
     * @param user the user to find token for
     * @param now current timestamp to check expiration
     * @return Optional containing valid token if found
     */
    @Query("SELECT t FROM OAuthToken t WHERE t.user = :user AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    Optional<OAuthToken> findValidTokenByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Find tokens that are expiring soon (within specified minutes).
     * @param thresholdTime the time threshold for expiration
     * @return list of tokens expiring soon
     */
    @Query("SELECT t FROM OAuthToken t WHERE t.expiresAt <= :thresholdTime AND t.expiresAt > CURRENT_TIMESTAMP")
    List<OAuthToken> findTokensExpiringSoon(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * Find all expired tokens.
     * @param now current timestamp
     * @return list of expired tokens
     */
    @Query("SELECT t FROM OAuthToken t WHERE t.expiresAt <= :now")
    List<OAuthToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete expired tokens (cleanup operation).
     * @param now current timestamp
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM OAuthToken t WHERE t.expiresAt <= :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete all tokens for a user (for account deletion).
     * @param user the user whose tokens should be deleted
     * @return number of deleted tokens
     */
    long deleteByUser(User user);

    /**
     * Check if user has any valid OAuth tokens.
     * @param user the user to check
     * @param now current timestamp
     * @return true if user has at least one valid token
     */
    @Query("SELECT COUNT(t) > 0 FROM OAuthToken t WHERE t.user = :user AND t.expiresAt > :now")
    boolean hasValidToken(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Count total tokens for a user.
     * @param user the user to count tokens for
     * @return number of tokens for the user
     */
    long countByUser(User user);

    /**
     * Find tokens created after a specific date.
     * @param user the user to find tokens for
     * @param since the date to search from
     * @return list of tokens created after the date
     */
    @Query("SELECT t FROM OAuthToken t WHERE t.user = :user AND t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<OAuthToken> findTokensCreatedAfter(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * Update token expiration time.
     * @param tokenId the ID of the token to update
     * @param newExpirationTime the new expiration time
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE OAuthToken t SET t.expiresAt = :newExpirationTime, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :tokenId")
    int updateTokenExpiration(@Param("tokenId") Long tokenId, @Param("newExpirationTime") LocalDateTime newExpirationTime);
}