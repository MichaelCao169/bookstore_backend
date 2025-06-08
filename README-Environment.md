# Environment Variables Setup

## Overview
This project uses environment variables to store sensitive configuration data like API keys, passwords, and secrets. This ensures that sensitive information is not committed to version control.

## Setup Instructions

### 1. Create .env file
Copy the `.env.example` file to `.env`:
```bash
cp .env.example .env
```

### 2. Configure your environment variables
Edit the `.env` file and replace the placeholder values with your actual configuration:

```bash
# Database Configuration
DB_PASSWORD=your_actual_database_password

# Email Configuration  
EMAIL_PASSWORD=your_actual_gmail_app_password

# JWT Configuration
JWT_SECRET=your_actual_jwt_secret_key

# OpenAI API Configuration
OPENAI_API_KEY=your_actual_openai_api_key
```

### 3. Environment Variables Description

- **DB_PASSWORD**: Your MySQL database password
- **EMAIL_PASSWORD**: Your Gmail App Password (not your regular Gmail password)
- **JWT_SECRET**: A secure random string for JWT token signing
- **OPENAI_API_KEY**: Your OpenAI API key for AI chat functionality

### 4. Important Notes

- The `.env` file is automatically ignored by git (see `.gitignore`)
- Never commit the `.env` file to version control
- Always use the `.env.example` file as a template for new developers
- For production deployment, set these variables in your deployment environment

### 5. Getting API Keys

#### OpenAI API Key
1. Go to [OpenAI Platform](https://platform.openai.com/)
2. Create an account or sign in
3. Navigate to API Keys section
4. Generate a new API key
5. Copy the key to your `.env` file

#### Gmail App Password
1. Enable 2-factor authentication on your Gmail account
2. Go to your Google Account settings
3. Security → 2-Step Verification → App passwords
4. Generate an app password for "Mail"
5. Use this password in the EMAIL_PASSWORD variable

## Troubleshooting

If you encounter issues with environment variables:
1. Make sure the `.env` file is in the root directory of the backend project
2. Restart your Spring Boot application after modifying the `.env` file
3. Check that the `spring-dotenv` dependency is properly installed 