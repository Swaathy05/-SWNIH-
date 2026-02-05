# 📧 Gmail Integration Setup Guide

To connect real Gmail accounts and fetch actual email data, you need to set up Google OAuth credentials.

## 🔧 Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the **Gmail API**:
   - Go to "APIs & Services" > "Library"
   - Search for "Gmail API"
   - Click "Enable"

## 🔑 Step 2: Create OAuth Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth 2.0 Client IDs"
3. Configure OAuth consent screen if prompted:
   - User Type: External (for testing)
   - App name: "Smart Web Notification Intelligence Hub"
   - User support email: Your email
   - Developer contact: Your email
4. Create OAuth 2.0 Client ID:
   - Application type: Web application
   - Name: "SWNIH Web Client"
   - Authorized redirect URIs: `http://localhost:8080/api/gmail/oauth/callback`

## ⚠️ CRITICAL: Add Test Users (Required for OAuth)

**If you get "Error 403: access_denied", you MUST add your email as a test user:**

1. Go to "APIs & Services" > "OAuth consent screen"
2. Scroll down to "Test users" section
3. Click "ADD USERS"
4. Add your email address: `swaathybalaji55@gmail.com`
5. Click "SAVE"

**This step is MANDATORY when your app is in testing mode!**

## 📝 Step 3: Update Configuration

Copy your credentials and update `src/main/resources/application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID_HERE
            client-secret: YOUR_GOOGLE_CLIENT_SECRET_HERE
            scope:
              - openid
              - profile
              - email
              - https://www.googleapis.com/auth/gmail.readonly
            redirect-uri: "http://localhost:8080/api/gmail/oauth/callback"
```

## 🚀 Step 4: Test Gmail Connection

1. Restart your application
2. Login to your SWNIH account
3. Click "Connect Gmail" button
4. Authorize the application in the popup
5. Your real Gmail messages will be fetched and classified!

## 🔒 Security Notes

- **Client Secret**: Keep your client secret secure and never commit it to version control
- **Scopes**: We only request `gmail.readonly` permission for security
- **Tokens**: All OAuth tokens are encrypted using AES-256 before storage
- **Revocation**: Users can disconnect Gmail anytime from the dashboard

## 🧪 Testing Without Real Gmail

If you don't want to set up Google OAuth right now, the application will continue showing demo data. The "Connect Gmail" button will show a message that OAuth credentials are not configured.

## 📊 Message Classification

Once connected, your real Gmail messages will be automatically classified:

- **HIGH Priority**: interview, offer, urgent, deadline, exam, emergency, important, asap, critical
- **MEDIUM Priority**: meeting, reminder, schedule, appointment, update, notification, alert  
- **LOW Priority**: sale, discount, promotion, newsletter, unsubscribe, marketing

## 🔧 Troubleshooting

**"Error 403: access_denied"**: 
- **SOLUTION**: Go to Google Cloud Console > APIs & Services > OAuth consent screen
- Scroll to "Test users" section and click "ADD USERS"
- Add your email: `swaathybalaji55@gmail.com`
- Click "SAVE" and try connecting Gmail again

**"OAuth credentials not configured"**: Update application.yml with your Google credentials
**"Gmail is not connected"**: Click "Connect Gmail" and complete OAuth flow
**"Failed to fetch messages"**: Check that Gmail API is enabled and credentials are correct

---

**🎉 Once configured, you'll have a fully functional Gmail integration with real-time message classification!**