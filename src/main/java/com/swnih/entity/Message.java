package com.swnih.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Message entity representing processed email messages with priority classification.
 * Stores message content, sender information, and ML classification results.
 */
@Entity
@Table(name = "messages", 
    indexes = {
        @Index(name = "idx_user_priority", columnList = "user_id, priority"),
        @Index(name = "idx_user_timestamp", columnList = "user_id, timestamp"),
        @Index(name = "idx_sender", columnList = "sender"),
        @Index(name = "idx_priority", columnList = "priority"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_message", columnNames = {"user_id", "sender", "subject", "timestamp"})
    }
)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Column(nullable = false)
    @NotBlank(message = "Sender is required")
    private String sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Subject is required")
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Body is required")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Priority is required")
    private PriorityLevel priority;

    @Column(length = 50)
    private String source = "GMAIL";

    @Column(name = "ml_confidence", precision = 3, scale = 2)
    @DecimalMin(value = "0.00", message = "ML confidence must be between 0.00 and 1.00")
    @DecimalMax(value = "1.00", message = "ML confidence must be between 0.00 and 1.00")
    private BigDecimal mlConfidence;

    @Column(nullable = false)
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Message() {}

    public Message(User user, String sender, String subject, String body, 
                   PriorityLevel priority, LocalDateTime timestamp) {
        this.user = user;
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.priority = priority;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public BigDecimal getMlConfidence() {
        return mlConfidence;
    }

    public void setMlConfidence(BigDecimal mlConfidence) {
        this.mlConfidence = mlConfidence;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public String getFullContent() {
        return subject + " " + body;
    }

    public boolean isHighPriority() {
        return priority == PriorityLevel.HIGH;
    }

    public boolean isMediumPriority() {
        return priority == PriorityLevel.MEDIUM;
    }

    public boolean isLowPriority() {
        return priority == PriorityLevel.LOW;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return id != null && id.equals(message.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", sender='" + sender + '\'' +
                ", subject='" + subject + '\'' +
                ", priority=" + priority +
                ", timestamp=" + timestamp +
                '}';
    }
}