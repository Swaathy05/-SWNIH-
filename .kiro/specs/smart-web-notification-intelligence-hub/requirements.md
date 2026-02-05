# Requirements Document

## Introduction

The Smart Web Notification Intelligence Hub (SWNIH) is a unified priority-based notification management system that addresses the critical problem of users missing important notifications among spam and low-priority messages. The system automatically fetches Gmail messages, classifies their importance using NLP and machine learning, and presents them in a prioritized dashboard interface.

## Glossary

- **SWNIH_System**: The Smart Web Notification Intelligence Hub platform
- **User**: A registered individual who uses the system to manage notifications
- **Gmail_Integration**: OAuth 2.0 connection to Gmail API for message retrieval
- **Priority_Classifier**: Combined keyword-based and ML classification engine
- **Dashboard**: Web interface displaying prioritized notifications
- **Message**: Email content fetched from Gmail including subject, body, and metadata
- **Priority_Level**: Classification of HIGH, MEDIUM, or LOW importance
- **ML_Service**: Python-based machine learning classification service
- **Authentication_Service**: JWT-based user authentication system

## Requirements

### Requirement 1: User Authentication

**User Story:** As a user, I want to register and login securely, so that I can access my personalized notification dashboard.

#### Acceptance Criteria

1. WHEN a user provides valid registration details (username, email, password), THE Authentication_Service SHALL create a new user account with encrypted password storage
2. WHEN a user provides valid login credentials, THE Authentication_Service SHALL generate a JWT token valid for 24 hours
3. WHEN a user provides invalid credentials, THE Authentication_Service SHALL reject the login attempt and return an appropriate error message
4. THE Authentication_Service SHALL enforce password requirements of minimum 8 characters with at least one uppercase, lowercase, and numeric character
5. WHEN a JWT token expires, THE SWNIH_System SHALL require re-authentication before allowing access to protected resources

### Requirement 2: Gmail Integration

**User Story:** As a user, I want to connect my Gmail account, so that the system can automatically fetch and analyze my messages.

#### Acceptance Criteria

1. WHEN a user initiates Gmail connection, THE Gmail_Integration SHALL redirect to Google OAuth 2.0 authorization flow
2. WHEN OAuth authorization is successful, THE Gmail_Integration SHALL store encrypted access tokens securely in the database
3. WHEN fetching messages, THE Gmail_Integration SHALL retrieve the most recent 50 messages from the user's Gmail inbox
4. WHEN Gmail API rate limits are exceeded, THE Gmail_Integration SHALL implement exponential backoff retry logic
5. WHEN access tokens expire, THE Gmail_Integration SHALL automatically refresh tokens using stored refresh tokens

### Requirement 3: Message Processing

**User Story:** As a system administrator, I want messages to be processed and cleaned, so that the classification algorithms receive high-quality input data.

#### Acceptance Criteria

1. WHEN a message is fetched from Gmail, THE SWNIH_System SHALL extract subject line, body content, sender information, and timestamp
2. WHEN processing message content, THE SWNIH_System SHALL remove HTML tags, normalize whitespace, and convert to UTF-8 encoding
3. WHEN message body exceeds 5000 characters, THE SWNIH_System SHALL truncate content while preserving the first 5000 characters
4. THE SWNIH_System SHALL store processed messages in the database with user_id, sender, subject, body, source, and timestamp fields
5. WHEN duplicate messages are detected (same subject and sender within 1 hour), THE SWNIH_System SHALL skip processing to avoid duplicates

### Requirement 4: Priority Classification

**User Story:** As a user, I want messages automatically classified by importance, so that I can focus on critical notifications first.

#### Acceptance Criteria

1. WHEN analyzing message content, THE Priority_Classifier SHALL apply keyword-based rules where interview/offer/exam/deadline keywords result in HIGH priority
2. WHEN analyzing message content, THE Priority_Classifier SHALL apply keyword-based rules where meeting/reminder keywords result in MEDIUM priority
3. WHEN analyzing message content, THE Priority_Classifier SHALL apply keyword-based rules where sale/discount/promotion keywords result in LOW priority
4. WHEN ML classification produces confidence score above 0.7, THE Priority_Classifier SHALL assign HIGH priority regardless of keyword results
5. WHEN ML classification produces confidence score above 0.4 but below 0.7 and no strong keywords are present, THE Priority_Classifier SHALL assign MEDIUM priority
6. WHEN ML classification produces confidence score below 0.4 and no strong keywords are present, THE Priority_Classifier SHALL assign LOW priority
7. THE ML_Service SHALL use TF-IDF vectorization combined with Logistic Regression or Naive Bayes classification
8. WHEN classification is complete, THE SWNIH_System SHALL store the priority level with the message record

### Requirement 5: Dashboard Interface

**User Story:** As a user, I want a unified dashboard showing prioritized notifications, so that I can quickly identify and act on important messages.

#### Acceptance Criteria

1. WHEN a user accesses the dashboard, THE SWNIH_System SHALL display messages organized in HIGH, MEDIUM, and LOW priority panels
2. WHEN displaying messages, THE SWNIH_System SHALL show sender, subject, timestamp, and priority level for each message
3. WHEN a user searches for messages, THE SWNIH_System SHALL filter results based on sender, subject, or content matching the search query
4. WHEN a user applies priority filters, THE SWNIH_System SHALL display only messages matching the selected priority levels
5. WHEN a user clicks on a message, THE SWNIH_System SHALL display the full message content in a modal or expanded view
6. THE SWNIH_System SHALL display messages in reverse chronological order within each priority panel
7. WHEN the dashboard loads, THE SWNIH_System SHALL show the most recent 100 messages by default with pagination for older messages

### Requirement 6: Data Persistence

**User Story:** As a system administrator, I want reliable data storage, so that user information and messages are preserved and accessible.

#### Acceptance Criteria

1. THE SWNIH_System SHALL store user accounts in a User table with id, username, email, and password_hash fields
2. THE SWNIH_System SHALL store messages in a Message table with id, user_id, sender, subject, body, priority, source, and timestamp fields
3. WHEN storing passwords, THE SWNIH_System SHALL use bcrypt hashing with minimum 12 rounds
4. WHEN storing OAuth tokens, THE SWNIH_System SHALL encrypt access and refresh tokens using AES-256 encryption
5. THE SWNIH_System SHALL implement database connection pooling with minimum 5 and maximum 20 concurrent connections
6. WHEN database operations fail, THE SWNIH_System SHALL log errors and return appropriate error responses to users

### Requirement 7: API Endpoints

**User Story:** As a frontend developer, I want well-defined API endpoints, so that I can build a responsive user interface.

#### Acceptance Criteria

1. THE SWNIH_System SHALL provide POST /api/auth/register endpoint accepting username, email, and password parameters
2. THE SWNIH_System SHALL provide POST /api/auth/login endpoint accepting email and password parameters and returning JWT token
3. THE SWNIH_System SHALL provide GET /api/gmail/connect endpoint initiating OAuth 2.0 flow for Gmail authorization
4. THE SWNIH_System SHALL provide GET /api/gmail/fetch endpoint triggering message retrieval from Gmail API
5. THE SWNIH_System SHALL provide GET /api/messages endpoint returning paginated list of user's messages
6. THE SWNIH_System SHALL provide GET /api/messages/{priority} endpoint returning messages filtered by priority level
7. WHEN API endpoints receive requests, THE SWNIH_System SHALL validate JWT tokens for protected endpoints and return 401 for invalid tokens
8. WHEN API endpoints encounter errors, THE SWNIH_System SHALL return appropriate HTTP status codes with descriptive error messages

### Requirement 8: Security and Privacy

**User Story:** As a user, I want my data protected and secure, so that my personal information and messages remain private.

#### Acceptance Criteria

1. THE SWNIH_System SHALL enforce HTTPS for all client-server communication
2. WHEN handling OAuth tokens, THE SWNIH_System SHALL store them encrypted and never expose them in API responses
3. THE SWNIH_System SHALL implement CORS policies restricting cross-origin requests to authorized domains
4. WHEN users request account deletion, THE SWNIH_System SHALL permanently remove all associated user data and messages
5. THE SWNIH_System SHALL implement rate limiting of 100 requests per minute per user to prevent abuse
6. WHEN logging system events, THE SWNIH_System SHALL exclude sensitive information like passwords and tokens from log files

### Requirement 9: Machine Learning Service

**User Story:** As a system architect, I want a dedicated ML service, so that message classification can be performed efficiently and accurately.

#### Acceptance Criteria

1. THE ML_Service SHALL implement TF-IDF vectorization for text feature extraction from message content
2. THE ML_Service SHALL train classification models using Logistic Regression or Naive Bayes algorithms
3. WHEN receiving classification requests, THE ML_Service SHALL return confidence scores between 0.0 and 1.0
4. THE ML_Service SHALL provide REST API endpoint accepting message text and returning priority classification
5. WHEN ML models require retraining, THE ML_Service SHALL support model updates without system downtime
6. THE ML_Service SHALL process classification requests within 500 milliseconds for optimal user experience

### Requirement 10: System Performance

**User Story:** As a user, I want fast and responsive system performance, so that I can efficiently manage my notifications.

#### Acceptance Criteria

1. WHEN users access the dashboard, THE SWNIH_System SHALL load and display messages within 2 seconds
2. WHEN fetching messages from Gmail, THE SWNIH_System SHALL complete the operation within 10 seconds for 50 messages
3. WHEN classifying message priority, THE SWNIH_System SHALL complete processing within 1 second per message
4. THE SWNIH_System SHALL support concurrent access by up to 100 users without performance degradation
5. WHEN database queries are executed, THE SWNIH_System SHALL use appropriate indexes to ensure response times under 100 milliseconds