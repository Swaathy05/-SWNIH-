-- Smart Web Notification Intelligence Hub Database Schema
-- This script creates the initial database schema for the SWNIH system

-- Create database (run this separately if needed)
-- CREATE DATABASE IF NOT EXISTS swnih_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE swnih_db;

-- Users table - stores user account information
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for performance
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OAuth tokens table - stores encrypted OAuth access and refresh tokens
CREATE TABLE oauth_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    access_token_encrypted TEXT NOT NULL,
    refresh_token_encrypted TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Messages table - stores processed email messages with priority classification
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    sender VARCHAR(255) NOT NULL,
    subject TEXT NOT NULL,
    body TEXT NOT NULL,
    priority ENUM('HIGH', 'MEDIUM', 'LOW') NOT NULL,
    source VARCHAR(50) DEFAULT 'GMAIL',
    ml_confidence DECIMAL(3,2),
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes for performance optimization
    INDEX idx_user_priority (user_id, priority),
    INDEX idx_user_timestamp (user_id, timestamp DESC),
    INDEX idx_sender (sender),
    INDEX idx_priority (priority),
    INDEX idx_timestamp (timestamp DESC),
    
    -- Unique constraint to prevent duplicate messages
    UNIQUE KEY unique_message (user_id, sender, subject, timestamp),
    
    -- Full-text search index for message content
    FULLTEXT INDEX idx_fulltext_search (subject, body)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create a view for message statistics (optional, for monitoring)
CREATE VIEW message_stats AS
SELECT 
    u.username,
    COUNT(m.id) as total_messages,
    COUNT(CASE WHEN m.priority = 'HIGH' THEN 1 END) as high_priority,
    COUNT(CASE WHEN m.priority = 'MEDIUM' THEN 1 END) as medium_priority,
    COUNT(CASE WHEN m.priority = 'LOW' THEN 1 END) as low_priority,
    MAX(m.timestamp) as latest_message,
    MIN(m.timestamp) as earliest_message
FROM users u
LEFT JOIN messages m ON u.id = m.user_id
GROUP BY u.id, u.username;

-- Insert sample data for development (optional)
-- This can be removed in production
INSERT INTO users (username, email, password_hash) VALUES 
('testuser', 'test@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBLzVernV1jDdW'); -- password: password123

-- Add some constraints and triggers for data integrity
DELIMITER //

-- Trigger to update the updated_at timestamp
CREATE TRIGGER update_users_timestamp 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER update_oauth_tokens_timestamp 
    BEFORE UPDATE ON oauth_tokens 
    FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER update_messages_timestamp 
    BEFORE UPDATE ON messages 
    FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

DELIMITER ;