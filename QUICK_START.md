# 🚀 Quick Start Guide - Smart Web Notification Intelligence Hub

## Prerequisites

Before running the application, make sure you have:

1. **Java 17** or higher installed
2. **Maven 3.6** or higher installed
3. **MySQL 8.0** or higher running

## Step 1: Database Setup

### Option A: Using MySQL Command Line
```bash
# Connect to MySQL as root
mysql -u root -p

# Create database and user
CREATE DATABASE swnih_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'swnih_user'@'localhost' IDENTIFIED BY 'swnih_password';
GRANT ALL PRIVILEGES ON swnih_db.* TO 'swnih_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# Run the schema
mysql -u swnih_user -p swnih_db < src/main/resources/db/migration/V1__Create_initial_schema.sql
```

### Option B: Using H2 Database (For Testing)
If you don't have MySQL, you can run with H2 in-memory database:
```bash
mvn spring-boot:run -Dspring.profiles.active=test
```

## Step 2: Run the Application

### Quick Start (with H2 database)
```bash
mvn spring-boot:run -Dspring.profiles.active=test
```

### With MySQL Database
```bash
mvn spring-boot:run
```

### With Custom Configuration
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

## Step 3: Access the Application

Once the application starts, you can access it at:

**🌐 Web Interface:** http://localhost:8080

**📡 API Base URL:** http://localhost:8080/api

## Step 4: Test the API

### Register a New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com", 
    "password": "Password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123"
  }'
```

### Check Health
```bash
curl http://localhost:8080/api/health
```

## Available Endpoints

### 🔐 Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### 📧 Gmail Integration  
- `GET /api/gmail/connect` - Connect Gmail (requires JWT token)
- `GET /api/gmail/status` - Check Gmail connection status
- `DELETE /api/gmail/disconnect` - Disconnect Gmail

### 📊 Health & Monitoring
- `GET /api/health` - Application health check
- `GET /actuator/health` - Detailed health information

## Configuration Options

### Environment Variables
```bash
# Database
export DB_USERNAME=swnih_user
export DB_PASSWORD=swnih_password

# JWT Secret (generate a secure key)
export JWT_SECRET=your-super-secure-jwt-secret-key-at-least-256-bits

# Google OAuth (for Gmail integration)
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret

# Encryption (for OAuth tokens)
export ENCRYPTION_SECRET_KEY=your-32-character-encryption-key
```

### Application Profiles
- `test` - H2 in-memory database, debug logging
- `dev` - MySQL database, debug logging  
- `prod` - MySQL database, production settings

## Troubleshooting

### Common Issues

1. **Port 8080 already in use**
   ```bash
   mvn spring-boot:run -Dserver.port=8081
   ```

2. **Database connection failed**
   - Check MySQL is running: `sudo service mysql status`
   - Verify credentials in application.yml
   - Ensure database exists: `SHOW DATABASES;`

3. **Build failures**
   ```bash
   mvn clean install -DskipTests
   ```

4. **Permission denied on MySQL**
   ```sql
   GRANT ALL PRIVILEGES ON swnih_db.* TO 'swnih_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

### Logs Location
- Application logs: `logs/swnih.log`
- Console output shows startup information

## What's Working Now

✅ **User Registration & Authentication** - JWT-based auth system
✅ **Gmail OAuth Integration** - Connect Gmail accounts securely  
✅ **Database Schema** - Users, OAuth tokens, messages tables
✅ **Security** - BCrypt passwords, AES-256 token encryption
✅ **API Endpoints** - RESTful APIs for all operations
✅ **Health Monitoring** - Application health checks

## Next Steps

To complete the full system:
1. **Gmail Message Fetching** - Retrieve actual emails
2. **ML Classification Service** - Python service for priority detection
3. **Message Processing Pipeline** - Clean and classify messages
4. **Web Dashboard** - Frontend interface for viewing messages

## Need Help?

- Check application logs: `tail -f logs/swnih.log`
- Verify database connection: `GET /api/health`
- Test authentication: Use the curl commands above
- Check Spring Boot startup logs for any errors

**🎉 Your Smart Web Notification Intelligence Hub is ready to run!**