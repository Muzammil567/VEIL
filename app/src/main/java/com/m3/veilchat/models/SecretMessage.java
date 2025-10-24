package com.m3.veilchat.models;

public class SecretMessage {
    private String messageId;
    private String senderId;
    private String senderPersonaId;
    private String content;
    private long timestamp;
    private boolean isLocked;
    private String lockPassword;
    private boolean isEncrypted;
    private String encryptionKey;
    private long expiresAt;
    private boolean isSelfDestruct;
    private int readCount;
    private boolean isReadOnce;
    private String messageType; // "text", "image", "file"

    public SecretMessage() {
        this.timestamp = System.currentTimeMillis();
        this.isLocked = false;
        this.isEncrypted = true;
        this.expiresAt = -1; // No expiration by default
        this.isSelfDestruct = false;
        this.readCount = 0;
        this.isReadOnce = false;
        this.messageType = "text";
    }

    public SecretMessage(String senderId, String content) {
        this();
        this.senderId = senderId;
        this.content = content;
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderPersonaId() { return senderPersonaId; }
    public void setSenderPersonaId(String senderPersonaId) { this.senderPersonaId = senderPersonaId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public String getLockPassword() { return lockPassword; }
    public void setLockPassword(String lockPassword) { this.lockPassword = lockPassword; }

    public boolean isEncrypted() { return isEncrypted; }
    public void setEncrypted(boolean encrypted) { isEncrypted = encrypted; }

    public String getEncryptionKey() { return encryptionKey; }
    public void setEncryptionKey(String encryptionKey) { this.encryptionKey = encryptionKey; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public boolean isSelfDestruct() { return isSelfDestruct; }
    public void setSelfDestruct(boolean selfDestruct) { isSelfDestruct = selfDestruct; }

    public int getReadCount() { return readCount; }
    public void setReadCount(int readCount) { this.readCount = readCount; }

    public boolean isReadOnce() { return isReadOnce; }
    public void setReadOnce(boolean readOnce) { isReadOnce = readOnce; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    // Helper methods
    public boolean hasExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }

    public boolean canBeRead() {
        if (isReadOnce && readCount >= 1) {
            return false;
        }
        return !hasExpired();
    }

    public void incrementReadCount() {
        this.readCount++;
    }

    public boolean requiresPassword() {
        return isLocked && lockPassword != null && !lockPassword.isEmpty();
    }
}