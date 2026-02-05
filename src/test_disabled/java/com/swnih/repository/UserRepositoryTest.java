package com.swnih.repository;

import com.swnih.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository.
 * Tests repository methods against an in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and retrieve user with all fields")
    void shouldSaveAndRetrieveUserWithAllFields() {
        // Given
        User newUser = new User("newuser", "new@example.com", "hashedPasswordNew");

        // When
        User savedUser = userRepository.save(newUser);
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(retrievedUser).isPresent();
        User user = retrievedUser.get();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("newuser");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashedPasswordNew");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        User testUser = new User("testuser1", "test1@example.com", "hashedPassword123");
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByEmail("test1@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser1");
        assertThat(found.get().getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        // Given
        User testUser = new User("testuser1", "test1@example.com", "hashedPassword123");
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByUsername("testuser1");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser1");
        assertThat(found.get().getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by username")
    void shouldReturnEmptyWhenUserNotFoundByUsername() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void shouldCheckIfUserExistsByEmail() {
        // Given
        User testUser = new User("testuser1", "test1@example.com", "hashedPassword123");
        entityManager.persistAndFlush(testUser);

        // When & Then
        assertThat(userRepository.existsByEmail("test1@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should check if user exists by username")
    void shouldCheckIfUserExistsByUsername() {
        // Given
        User testUser = new User("testuser1", "test1@example.com", "hashedPassword123");
        entityManager.persistAndFlush(testUser);

        // When & Then
        assertThat(userRepository.existsByUsername("testuser1")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should find user by email or username (case-insensitive)")
    void shouldFindUserByEmailOrUsernameCaseInsensitive() {
        // Given
        User testUser = new User("testuser1", "test1@example.com", "hashedPassword123");
        entityManager.persistAndFlush(testUser);

        // When - test with different cases
        Optional<User> foundByEmail = userRepository.findByEmailOrUsername("TEST1@EXAMPLE.COM", "");
        Optional<User> foundByUsername = userRepository.findByEmailOrUsername("", "TESTUSER1");

        // Then
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getUsername()).isEqualTo("testuser1");
        
        assertThat(foundByUsername).isPresent();
        assertThat(foundByUsername.get().getUsername()).isEqualTo("testuser1");
    }

    @Test
    @DisplayName("Should return empty when neither email nor username found")
    void shouldReturnEmptyWhenNeitherEmailNorUsernameFound() {
        // When
        Optional<User> found = userRepository.findByEmailOrUsername("nonexistent@example.com", "nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if email or username exists")
    void shouldCheckIfEmailOrUsernameExists() {
        // Given
        User testUser = new User("testuser1", "test1@example.com", "hashedPassword123");
        entityManager.persistAndFlush(testUser);

        // When & Then
        assertThat(userRepository.existsByEmailOrUsername("test1@example.com", "anyusername")).isTrue();
        assertThat(userRepository.existsByEmailOrUsername("anyemail@example.com", "testuser1")).isTrue();
        assertThat(userRepository.existsByEmailOrUsername("nonexistent@example.com", "nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should count total users")
    void shouldCountTotalUsers() {
        // Given
        User testUser1 = new User("testuser1", "test1@example.com", "hashedPassword123");
        User testUser2 = new User("testuser2", "test2@example.com", "hashedPassword456");
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);

        // When
        long count = userRepository.countTotalUsers();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find users created after specific date")
    void shouldFindUsersCreatedAfterSpecificDate() {
        // Given - create users
        User testUser1 = new User("testuser1", "test1@example.com", "hashedPassword123");
        User testUser2 = new User("testuser2", "test2@example.com", "hashedPassword456");
        User recentUser = new User("recentuser", "recent@example.com", "hashedPassword789");
        
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.persistAndFlush(recentUser);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(1);

        // When
        List<User> recentUsers = userRepository.findUsersCreatedAfter(cutoffDate);

        // Then
        assertThat(recentUsers).hasSize(3); // All users should be recent in this test
        assertThat(recentUsers).extracting(User::getUsername)
            .contains("testuser1", "testuser2", "recentuser");
    }

    @Test
    @DisplayName("Should update user and modify updated timestamp")
    void shouldUpdateUserAndModifyUpdatedTimestamp() {
        // Given
        User user = new User("testuser1", "test1@example.com", "hashedPassword123");
        entityManager.persistAndFlush(user);
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();
        
        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        user.setEmail("updated@example.com");
        User updatedUser = userRepository.save(user);

        // Then
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // Given
        User user = new User("testuser1", "test1@example.com", "hashedPassword123");
        entityManager.persistAndFlush(user);
        Long userId = user.getId();

        // When
        userRepository.delete(user);

        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }
}