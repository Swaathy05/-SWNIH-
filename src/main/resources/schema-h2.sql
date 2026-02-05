-- H2 Database Schema for Smart Web Notification Intelligence Hub
-- This script creates the initial database schema compatible with H2

-- Users table - stores user account information
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for users table
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_email ON users(email);

-- OAuth tokens table - stores encrypted OAuth access and refresh tokens
CREATE TABLE oauth_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    access_token_encrypted CLOB NOT NULL,
    refresh_token_encrypted CLOB NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_oauth_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for oauth_tokens table
CREATE INDEX idx_oauth_user_id ON oauth_tokens(user_id);
CREATE INDEX idx_oauth_expires_at ON oauth_tokens(expires_at);

-- Messages table - stores processed email messages with priority classification
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sender VARCHAR(255) NOT NULL,
    subject CLOB NOT NULL,
    body CLOB NOT NULL,
    priority VARCHAR(10) NOT NULL CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW')),
    source VARCHAR(50) DEFAULT 'GMAIL',
    ml_confidence DECIMAL(3,2),
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_messages_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for messages table
CREATE INDEX idx_messages_user_priority ON messages(user_id, priority);
CREATE INDEX idx_messages_user_timestamp ON messages(user_id, timestamp DESC);
CREATE INDEX idx_messages_sender ON messages(sender);
CREATE INDEX idx_messages_priority ON messages(priority);
CREATE INDEX idx_messages_timestamp ON messages(timestamp DESC);

-- Insert sample data for testing
INSERT INTO users (username, email, password_hash) VALUES 
('testuser', 'test@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBLzVernV1jDdW');