# 🔒 Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

We take the security of VEIL Chat App seriously. If you believe you've found a security vulnerability, please follow these steps:

### 🔍 Vulnerability Reporting Process

1. **Do NOT disclose the vulnerability publicly**
2. **Email us at**: security@veilchat.com
3. **Include**:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

### ⏰ Response Timeline

- **24 hours**: Initial response to your report
- **72 hours**: Vulnerability assessment completion  
- **7-14 days**: Fix development and testing
- **30 days**: Public disclosure (if appropriate)

### 🛡️ Security Measures

#### Encryption
- **AES-256** for message encryption
- **TLS 1.3** for network transport
- **Android Keystore** for key storage
- **EncryptedSharedPreferences** for local data

#### Authentication
- **Anonymous auth** as primary method
- **Email linking** for recovery only
- **App lock** with secure hashing
- **Session management** with expiration

#### Data Protection
- **Minimum data collection** principle
- **Automatic expiration** of temporary data
- **Secure cleanup** of deleted content
- **No personal information** storage

### 🚨 Security Features

#### Message Security
- End-to-end encryption for private messages
- Emoji cipher for visual security
- Blink messages with read-once detection
- Self-destructing rooms and chats

#### Identity Protection
- Multiple persona system
- Alias rotation and masking
- Trust-based identity revelation
- No permanent user identifiers

#### Platform Security
- Screenshot protection for sensitive content
- Certificate pinning
- Input validation and sanitization
- Regular security updates

### 📝 Security Best Practices for Users

1. **Use strong app lock passwords**
2. **Share cipher keys securely**
3. **Regularly update the app**
4. **Use different personas for different contexts**
5. **Be cautious with identity revelation**

### 🔄 Security Updates

We regularly:
- Update dependencies for security patches
- Conduct security audits
- Implement new security features
- Monitor for new vulnerabilities

### 📚 Security Documentation

- [Encryption Implementation](docs/encryption.md)
- [Data Protection](docs/data-protection.md)
- [Privacy Policy](PRIVACY.md)
