# Implementation Plan: Smart Web Notification Intelligence Hub

## Overview

This implementation plan breaks down the SWNIH system into discrete coding tasks that build incrementally. The approach starts with core infrastructure, adds authentication and Gmail integration, implements the ML classification service, builds the message processing pipeline, creates the web dashboard, and concludes with comprehensive testing and integration.

## Tasks

- [x] 1. Set up project structure and core infrastructure
  - Create Spring Boot project with Maven/Gradle configuration
  - Set up MySQL database schema with tables for users, messages, and oauth_tokens
  - Configure database connection pooling (5-20 connections)
  - Set up basic project structure with packages for controllers, services, entities, and repositories
  - Configure application properties for database, security, and external APIs
  - _Requirements: 6.1, 6.2, 6.5_

- [ ]* 1.1 Write property test for database connection pooling
  - **Property 20: Database Connection Management**
  - **Validates: Requirements 6.5**

- [ ] 2. Implement user authentication system
  - [x] 2.1 Create User entity and repository with JPA annotations
    - Implement User entity with id, username, email, passwordHash fields
    - Create UserRepository interface extending JpaRepository
    - Add unique constraints and validation annotations
    - _Requirements: 6.1_

  - [x] 2.2 Implement authentication service with password hashing
    - Create AuthenticationService with bcrypt password hashing (12+ rounds)
    - Implement user registration with password validation (8+ chars, uppercase, lowercase, numeric)
    - Implement login functionality with credential validation
    - _Requirements: 1.1, 1.3, 1.4, 6.3_

  - [ ]* 2.3 Write property test for user registration and authentication
    - **Property 1: User Registration and Authentication**
    - **Validates: Requirements 1.1, 1.2, 1.4, 6.3**

  - [x] 2.4 Implement JWT token generation and validation
    - Create JwtTokenService for token generation with 24-hour expiration
    - Implement token validation and parsing
    - Configure Spring Security with JWT authentication filter
    - _Requirements: 1.2, 1.5_

  - [ ]* 2.5 Write property test for authentication security
    - **Property 2: Authentication Security**
    - **Validates: Requirements 1.3, 1.5, 7.7**

- [ ] 3. Implement OAuth 2.0 Gmail integration
  - [x] 3.1 Create OAuth token entity and encryption service
    - Create OAuthToken entity with encrypted access/refresh tokens
    - Implement AES-256 encryption service for token storage
    - Create OAuthTokenRepository with user relationship
    - _Requirements: 2.2, 6.4_

  - [x] 3.2 Implement Gmail OAuth flow
    - Create GmailIntegrationService with OAuth 2.0 flow initiation
    - Implement OAuth callback handling and token storage
    - Add automatic token refresh logic using refresh tokens
    - _Requirements: 2.1, 2.5_

  - [ ]* 3.3 Write property test for OAuth integration flow
    - **Property 3: OAuth Integration Flow**
    - **Validates: Requirements 2.1, 2.2, 2.5, 6.4**

  - [ ] 3.4 Implement Gmail API message fetching
    - Create Gmail API client with rate limit handling
    - Implement message fetching (50 most recent messages)
    - Add exponential backoff retry logic for rate limits
    - _Requirements: 2.3, 2.4_

  - [ ]* 3.5 Write property test for Gmail message fetching
    - **Property 4: Gmail Message Fetching**
    - **Validates: Requirements 2.3, 2.4**

- [ ] 4. Create Python ML classification service
  - [ ] 4.1 Set up Python ML service project structure
    - Create Python project with Flask/FastAPI framework
    - Set up requirements.txt with scikit-learn, pandas, numpy
    - Create project structure with classification, training, and API modules
    - _Requirements: 9.1, 9.2_

  - [ ] 4.2 Implement TF-IDF vectorization and ML models
    - Create MessageClassifier class with TF-IDF vectorization
    - Implement Logistic Regression and Naive Bayes models
    - Add model training functionality with sample data
    - _Requirements: 9.1, 9.2_

  - [ ] 4.3 Create ML service REST API
    - Implement POST /classify endpoint accepting message text
    - Return confidence scores between 0.0 and 1.0
    - Add error handling and input validation
    - _Requirements: 9.3, 9.4_

  - [ ]* 4.4 Write property test for ML service response format
    - **Property 8: ML Service Response Format**
    - **Validates: Requirements 9.3, 9.4**

- [ ] 5. Implement message processing pipeline
  - [ ] 5.1 Create Message entity and repository
    - Create Message entity with all required fields (id, user_id, sender, subject, body, priority, source, timestamp)
    - Add JPA relationships and indexes for performance
    - Create MessageRepository with custom query methods
    - _Requirements: 6.2_

  - [ ] 5.2 Implement message processing service
    - Create MessageProcessingService for Gmail message extraction
    - Implement HTML tag removal, whitespace normalization, UTF-8 conversion
    - Add message truncation to 5000 characters
    - Implement duplicate detection (same sender/subject within 1 hour)
    - _Requirements: 3.1, 3.2, 3.3, 3.5_

  - [ ]* 5.3 Write property test for message processing pipeline
    - **Property 5: Message Processing Pipeline**
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.5**

  - [ ] 5.4 Implement hybrid priority classification
    - Create PriorityClassifier with keyword-based rules
    - Define HIGH priority keywords (interview, offer, exam, deadline)
    - Define MEDIUM priority keywords (meeting, reminder)
    - Define LOW priority keywords (sale, discount, promotion)
    - Integrate ML service calls with confidence threshold logic
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

  - [ ]* 5.5 Write property test for keyword-based classification
    - **Property 6: Keyword-Based Classification**
    - **Validates: Requirements 4.1, 4.2, 4.3**

  - [ ]* 5.6 Write property test for ML confidence classification
    - **Property 7: ML Confidence Classification**
    - **Validates: Requirements 4.4, 4.5, 4.6**

  - [ ] 5.7 Implement complete message processing workflow
    - Wire together fetching, processing, classification, and storage
    - Add transaction management for data consistency
    - Store processed messages with priority levels in database
    - _Requirements: 3.4, 4.8_

  - [ ]* 5.8 Write property test for data persistence
    - **Property 9: Data Persistence**
    - **Validates: Requirements 3.4, 4.8, 6.1, 6.2**

- [ ] 6. Checkpoint - Ensure core backend functionality works
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Implement REST API endpoints
  - [ ] 7.1 Create authentication API controllers
    - Implement POST /api/auth/register endpoint with input validation
    - Implement POST /api/auth/login endpoint returning JWT tokens
    - Add proper error handling and HTTP status codes
    - _Requirements: 7.1, 7.2_

  - [ ] 7.2 Create Gmail integration API controllers
    - Implement GET /api/gmail/connect endpoint for OAuth initiation
    - Implement GET /api/gmail/fetch endpoint for message retrieval
    - Add JWT token validation for protected endpoints
    - _Requirements: 7.3, 7.4, 7.7_

  - [ ] 7.3 Create message API controllers
    - Implement GET /api/messages endpoint with pagination
    - Implement GET /api/messages/{priority} endpoint with priority filtering
    - Add search functionality for sender/subject/content filtering
    - _Requirements: 7.5, 7.6_

  - [ ]* 7.4 Write property test for API endpoint availability
    - **Property 14: API Endpoint Availability**
    - **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6**

  - [ ] 7.5 Implement comprehensive error handling
    - Add global exception handler for consistent error responses
    - Implement proper HTTP status codes for different error types
    - Add detailed error logging while excluding sensitive information
    - _Requirements: 6.6, 7.8, 8.6_

  - [ ]* 7.6 Write property test for error handling
    - **Property 15: Error Handling**
    - **Validates: Requirements 6.6, 7.8**

- [ ] 8. Implement security and privacy features
  - [ ] 8.1 Configure HTTPS and CORS policies
    - Configure Spring Security for HTTPS enforcement
    - Implement CORS configuration restricting to authorized domains
    - Ensure OAuth tokens are never exposed in API responses
    - _Requirements: 8.1, 8.2, 8.3_

  - [ ] 8.2 Implement rate limiting
    - Add rate limiting filter for 100 requests per minute per user
    - Implement user identification for rate limiting
    - Add appropriate error responses for rate limit violations
    - _Requirements: 8.5_

  - [ ] 8.3 Implement account deletion functionality
    - Create endpoint for user account deletion
    - Implement cascade deletion of all user data and messages
    - Add confirmation mechanism for account deletion
    - _Requirements: 8.4_

  - [ ]* 8.4 Write property test for security enforcement
    - **Property 16: Security Enforcement**
    - **Validates: Requirements 8.1, 8.2, 8.3**

  - [ ]* 8.5 Write property test for data privacy and deletion
    - **Property 17: Data Privacy and Deletion**
    - **Validates: Requirements 8.4**

  - [ ]* 8.6 Write property test for rate limiting
    - **Property 18: Rate Limiting**
    - **Validates: Requirements 8.5**

  - [ ]* 8.7 Write property test for secure logging
    - **Property 19: Secure Logging**
    - **Validates: Requirements 8.6**

- [ ] 9. Create web dashboard frontend
  - [ ] 9.1 Create HTML structure and CSS styling
    - Create index.html with login/register forms
    - Create dashboard.html with priority panels (HIGH/MEDIUM/LOW)
    - Implement responsive CSS for desktop and mobile
    - Add modal/expanded view for message details
    - _Requirements: 5.1, 5.5_

  - [ ] 9.2 Implement JavaScript for authentication
    - Create authentication functions for login/register
    - Implement JWT token storage and management
    - Add OAuth flow initiation for Gmail connection
    - Handle authentication errors and redirects
    - _Requirements: 1.1, 1.2, 1.3, 2.1_

  - [ ] 9.3 Implement dashboard functionality
    - Create functions to fetch and display messages by priority
    - Implement reverse chronological ordering within priority panels
    - Add pagination for loading older messages (100 messages default)
    - Display sender, subject, timestamp, and priority for each message
    - _Requirements: 5.1, 5.2, 5.6, 5.7_

  - [ ]* 9.4 Write property test for dashboard display organization
    - **Property 10: Dashboard Display Organization**
    - **Validates: Requirements 5.1, 5.2, 5.6**

  - [ ]* 9.5 Write property test for pagination and loading
    - **Property 13: Pagination and Loading**
    - **Validates: Requirements 5.7**

  - [ ] 9.6 Implement search and filtering functionality
    - Add search input for filtering by sender/subject/content
    - Implement priority filter checkboxes
    - Create real-time filtering of displayed messages
    - _Requirements: 5.3, 5.4_

  - [ ]* 9.7 Write property test for search and filtering
    - **Property 11: Search and Filtering**
    - **Validates: Requirements 5.3, 5.4**

  - [ ] 9.8 Implement message detail display
    - Add click handlers for message expansion
    - Create modal or expanded view for full message content
    - Implement close/collapse functionality
    - _Requirements: 5.5_

  - [ ]* 9.9 Write property test for message detail display
    - **Property 12: Message Detail Display**
    - **Validates: Requirements 5.5**

- [ ] 10. Integration and final wiring
  - [ ] 10.1 Connect frontend to backend APIs
    - Wire authentication forms to /api/auth endpoints
    - Connect Gmail integration to /api/gmail endpoints
    - Wire dashboard to /api/messages endpoints
    - Add proper error handling and user feedback
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

  - [ ] 10.2 Deploy and configure ML service
    - Deploy Python ML service with proper configuration
    - Configure Spring Boot to communicate with ML service
    - Add health checks and error handling for ML service communication
    - _Requirements: 9.3, 9.4_

  - [ ] 10.3 Configure production settings
    - Set up production database configuration
    - Configure OAuth client credentials for Gmail API
    - Set up HTTPS certificates and security headers
    - Configure logging and monitoring
    - _Requirements: 8.1, 8.3, 6.6_

- [ ] 11. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional property-based tests that can be skipped for faster MVP
- Each task references specific requirements for traceability
- The implementation follows the specified tech stack: Java Spring Boot, Python scikit-learn, HTML/CSS/JavaScript, MySQL
- Property tests validate universal correctness properties across all inputs
- Unit tests can be added for specific examples and edge cases as needed
- The ML service runs as a separate Python service for specialized ML capabilities
- OAuth integration requires Google API credentials configuration
- Database indexes are included for optimal query performance