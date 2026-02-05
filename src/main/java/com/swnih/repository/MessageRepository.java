package com.swnih.repository;

import com.swnih.entity.Message;
import com.swnih.entity.PriorityLevel;
import com.swnih.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Message entity operations.
 * Provides CRUD operations and custom queries for message management and filtering.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages for a specific user, ordered by timestamp descending.
     * @param user the user to find messages for
     * @param pageable pagination information
     * @return page of messages for the user
     */
    Page<Message> findByUserOrderByTimestampDesc(User user, Pageable pageable);

    /**
     * Find messages for a user with specific priority level.
     * @param user the user to find messages for
     * @param priority the priority level to filter by
     * @param pageable pagination information
     * @return page of messages matching the criteria
     */
    Page<Message> findByUserAndPriorityOrderByTimestampDesc(User user, PriorityLevel priority, Pageable pageable);

    /**
     * Find messages for a user by sender (case-insensitive).
     * @param user the user to find messages for
     * @param sender the sender to search for
     * @param pageable pagination information
     * @return page of messages from the specified sender
     */
    @Query("SELECT m FROM Message m WHERE m.user = :user AND LOWER(m.sender) LIKE LOWER(CONCAT('%', :sender, '%')) ORDER BY m.timestamp DESC")
    Page<Message> findByUserAndSenderContainingIgnoreCase(@Param("user") User user, @Param("sender") String sender, Pageable pageable);

    /**
     * Search messages by content (subject or body) using full-text search.
     * @param user the user to search messages for
     * @param searchTerm the term to search for in subject or body
     * @param pageable pagination information
     * @return page of messages matching the search criteria
     */
    @Query("SELECT m FROM Message m WHERE m.user = :user AND " +
           "(LOWER(m.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.body) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY m.timestamp DESC")
    Page<Message> searchByContent(@Param("user") User user, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find messages with multiple priority levels for a user.
     * @param user the user to find messages for
     * @param priorities list of priority levels to include
     * @param pageable pagination information
     * @return page of messages matching any of the specified priorities
     */
    Page<Message> findByUserAndPriorityInOrderByTimestampDesc(User user, List<PriorityLevel> priorities, Pageable pageable);

    /**
     * Check for duplicate messages (same sender, subject, and timestamp within threshold).
     * @param user the user to check for
     * @param sender the sender of the message
     * @param subject the subject of the message
     * @param timestampStart start of time window for duplicate detection
     * @param timestampEnd end of time window for duplicate detection
     * @return Optional containing existing message if duplicate found
     */
    @Query("SELECT m FROM Message m WHERE m.user = :user AND m.sender = :sender AND m.subject = :subject " +
           "AND m.timestamp BETWEEN :timestampStart AND :timestampEnd")
    Optional<Message> findDuplicateMessage(@Param("user") User user, 
                                         @Param("sender") String sender, 
                                         @Param("subject") String subject,
                                         @Param("timestampStart") LocalDateTime timestampStart,
                                         @Param("timestampEnd") LocalDateTime timestampEnd);

    /**
     * Count messages by priority level for a user.
     * @param user the user to count messages for
     * @param priority the priority level to count
     * @return number of messages with the specified priority
     */
    long countByUserAndPriority(User user, PriorityLevel priority);

    /**
     * Get message statistics for a user.
     * @param user the user to get statistics for
     * @return array containing [total, high, medium, low] message counts
     */
    @Query("SELECT COUNT(m), " +
           "SUM(CASE WHEN m.priority = 'HIGH' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN m.priority = 'MEDIUM' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN m.priority = 'LOW' THEN 1 ELSE 0 END) " +
           "FROM Message m WHERE m.user = :user")
    Object[] getMessageStatistics(@Param("user") User user);

    /**
     * Find recent messages for a user (within last N days).
     * @param user the user to find messages for
     * @param since the date to search from
     * @param pageable pagination information
     * @return page of recent messages
     */
    @Query("SELECT m FROM Message m WHERE m.user = :user AND m.timestamp >= :since ORDER BY m.timestamp DESC")
    Page<Message> findRecentMessages(@Param("user") User user, @Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Find messages with high ML confidence scores.
     * @param user the user to find messages for
     * @param minConfidence minimum confidence threshold
     * @param pageable pagination information
     * @return page of messages with high ML confidence
     */
    @Query("SELECT m FROM Message m WHERE m.user = :user AND m.mlConfidence >= :minConfidence ORDER BY m.mlConfidence DESC, m.timestamp DESC")
    Page<Message> findHighConfidenceMessages(@Param("user") User user, @Param("minConfidence") java.math.BigDecimal minConfidence, Pageable pageable);

    /**
     * Delete all messages for a user (for account deletion).
     * @param user the user whose messages should be deleted
     * @return number of deleted messages
     */
    long deleteByUser(User user);

    /**
     * Find messages by source (e.g., GMAIL).
     * @param user the user to find messages for
     * @param source the source to filter by
     * @param pageable pagination information
     * @return page of messages from the specified source
     */
    Page<Message> findByUserAndSourceOrderByTimestampDesc(User user, String source, Pageable pageable);
}