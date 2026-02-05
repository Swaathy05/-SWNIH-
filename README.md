# 🧠 SWNIH - Smart Web Notification Intelligence Hub

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gmail API](https://img.shields.io/badge/Gmail%20API-OAuth%202.0-red.svg)](https://developers.google.com/gmail/api)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **Transform your email chaos into organized intelligence with AI-powered prioritization**

SWNIH is a modern, full-stack email management platform that automatically prioritizes your Gmail messages using intelligent classification algorithms. Never miss important emails again!

## ✨ Features

### 🎯 **Core Functionality**
- **AI-Powered Email Classification** - Automatically sorts emails into High, Medium, and Low priority
- **Gmail Integration** - Secure OAuth 2.0 connection to your Gmail account
- **Real-time Dashboard** - Beautiful interface showing prioritized email columns
- **Smart Notifications** - Focus on what matters most

### 🔒 **Security & Authentication**
- **JWT Authentication** - Secure token-based user sessions
- **OAuth 2.0** - Safe Gmail access without storing passwords
- **Encrypted Token Storage** - AES encryption for sensitive data
- **BCrypt Password Hashing** - Industry-standard password security

### 🎨 **Modern User Experience**
- **Responsive Design** - Works perfectly on desktop, tablet, and mobile
- **Glass Morphism UI** - Modern, professional interface design
- **Smooth Animations** - Delightful user interactions
- **Dark/Light Themes** - Comfortable viewing in any environment

## 🚀 Quick Start

### Prerequisites
- **Java 17+** - [Download here](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** - [Installation guide](https://maven.apache.org/install.html)
- **Gmail API Credentials** - [Setup guide](GMAIL_SETUP.md)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Swaathy05/-SWNIH-.git
   cd -SWNIH-
   ```

2. **Configure Gmail OAuth** (See [GMAIL_SETUP.md](GMAIL_SETUP.md))
   ```yaml
   # Update src/main/resources/application.yml
   spring:
     security:
       oauth2:
         client:
           registration:
             google:
               client-id: your-client-id
               client-secret: your-client-secret
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Open your browser to `http://localhost:8080`
   - Register a new account or login
   - Connect your Gmail and start organizing!

## 🏗 Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   External      │
│   (Web App)     │◄──►│   (Spring Boot) │◄──►│   (Gmail API)   │
│                 │    │                 │    │                 │
│ • Dashboard     │    │ • REST APIs     │    │ • OAuth 2.0     │
│ • Auth Pages    │    │ • JWT Security  │    │ • Message Fetch │
│ • Responsive UI │    │ • Database      │    │ • Token Refresh │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠 Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Backend** | Java Spring Boot 3.2.1 | REST API, Business Logic |
| **Frontend** | HTML5, CSS3, JavaScript | User Interface |
| **Database** | H2 (Dev), MySQL (Prod) | Data Persistence |
| **Security** | Spring Security + JWT | Authentication & Authorization |
| **Email API** | Gmail API + OAuth 2.0 | Email Integration |
| **Build** | Maven | Dependency Management |
| **AI/ML** | Keyword Classification | Email Prioritization |

## 📊 API Documentation

### Authentication Endpoints
```http
POST /api/auth/register    # User registration
POST /api/auth/login       # User login
GET  /api/auth/validate    # Token validation
GET  /api/auth/me          # Current user info
```

### Gmail Integration Endpoints
```http
GET  /api/gmail/connect           # Initiate OAuth flow
GET  /api/gmail/oauth/callback    # OAuth callback handler
POST /api/gmail/exchange-code     # Exchange auth code for tokens
GET  /api/gmail/status            # Connection status
GET  /api/gmail/messages          # Fetch prioritized messages
DELETE /api/gmail/disconnect      # Revoke Gmail access
```

### Page Endpoints
```http
GET  /                    # Homepage
GET  /login              # Login page
GET  /register           # Registration page
GET  /dashboard          # Main dashboard
```

## 🎯 Email Classification Algorithm

SWNIH uses intelligent keyword analysis to classify emails:

### 🔴 **High Priority**
- Keywords: `interview`, `offer`, `urgent`, `deadline`, `emergency`, `critical`
- Examples: Job interviews, urgent deadlines, critical alerts

### 🟡 **Medium Priority**
- Keywords: `meeting`, `reminder`, `schedule`, `appointment`, `update`
- Examples: Team meetings, calendar reminders, project updates

### 🟢 **Low Priority**
- Keywords: `sale`, `discount`, `newsletter`, `promotion`, `marketing`
- Examples: Marketing emails, newsletters, social notifications

## 🔧 Configuration

### Environment Variables
```bash
# JWT Configuration
JWT_SECRET=your-jwt-secret-key-256-bits-minimum

# Database Configuration (Production)
DB_USERNAME=swnih_user
DB_PASSWORD=secure_password

# Gmail OAuth
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

### Application Properties
```yaml
# src/main/resources/application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:swnih_db
    username: sa
    password: password
  
jwt:
  secret: ${JWT_SECRET:your-jwt-secret-key-should-be-at-least-256-bits-long}
  expiration: 86400000 # 24 hours

gmail:
  api:
    max-messages: 50
    rate-limit:
      requests-per-second: 10
```

## 🧪 Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

### Manual Testing
1. Open `test-complete-app.html` in your browser
2. Run the complete application test suite
3. Verify all functionality works end-to-end

## 🚀 Deployment

### Local Development
```bash
mvn spring-boot:run
```

### Production Build
```bash
mvn clean package
java -jar target/smart-web-notification-intelligence-hub-0.0.1-SNAPSHOT.jar
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add amazing feature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### Development Guidelines
- Follow Java coding standards
- Write comprehensive tests
- Update documentation
- Ensure responsive design
- Test Gmail integration thoroughly

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring Boot Team** - For the excellent framework
- **Google Gmail API** - For email integration capabilities
- **JWT.io** - For JWT token standards
- **Modern CSS** - For design inspiration

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Swaathy05/-SWNIH-/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Swaathy05/-SWNIH-/discussions)
- **Email**: support@swnih.com

---

<div align="center">

**Made with ❤️ by [Swaathy05](https://github.com/Swaathy05)**

⭐ **Star this repo if you found it helpful!** ⭐

</div>