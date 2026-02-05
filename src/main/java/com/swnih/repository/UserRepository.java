package com.swnih.repository;

import com.swnih.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address.
     * @param email the email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username.
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists with the given email.
     * @param email the email address to check
     * @return true if user exists with this email
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user exists with the given username.
     * @param username the username to check
     * @return true if user exists with this username
     */
    boolean existsByUsername(String username);

    /**
     * Find user by email or username (case-insensitive).
     * @param email the email address to search for
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) OR LOWER(u.username) = LOWER(:username)")
    Optional<User> findByEmailOrUsername(@Param("email") String email, @Param("username") String username);

    /**
     * Check if email or username already exists (for registration validation).
     * @param email the email address to check
     * @param username the username to check
     * @return true if either email or username already exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email) OR LOWER(u.username) = LOWER(:username)")
    boolean existsByEmailOrUsername(@Param("email") String email, @Param("username") String username);

    /**
     * Count total number of users in the system.
     * @return total user count
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();

    /**
     * Find users created after a specific date (for analytics).
     * @param date the date to search from
     * @return list of users created after the date
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :date ORDER BY u.createdAt DESC")
    java.util.List<User> findUsersCreatedAfter(@Param("date") java.time.LocalDateTime date);
}