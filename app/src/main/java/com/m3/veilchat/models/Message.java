package com.m3.veilchat.models;

import java.util.ArrayList;
import java.util.List;

public class Message {
    private String messageId;
    private String roomId;
    private String senderId;
    private String senderPersonaId;
    private String senderDisplayName;
    private String content;
    private String messageType; // "text", "image", "system"
    private long timestamp;
    private long expiresAt; // For self-destructing messages
    private boolean isRead;
    private String rules; // "blink", "cipher", etc.
    private String cipherKey; // For encrypted messages

    private long readAt;
    private List<String> readByUserIds; // Track who read it

    // Constructors
    public Message() {
        this.timestamp = System.currentTimeMillis();
        this.messageType = "text";
        this.isRead = false;
    }

    public Message(String roomId, String senderId, String content) {
        this();
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
    }

    public boolean isReadOnce() {
        return "blink".equals(rules) && isRead;
    }

    public void markAsRead(String userId) {
        if (!isRead) {
            this.isRead = true;
            this.readAt = System.currentTimeMillis();

            if (readByUserIds == null) {
                readByUserIds = new ArrayList<>();
            }
            readByUserIds.add(userId);
        }
    }

    public boolean hasUserRead(String userId) {
        return readByUserIds != null && readByUserIds.contains(userId);
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderPersonaId() { return senderPersonaId; }
    public void setSenderPersonaId(String senderPersonaId) { this.senderPersonaId = senderPersonaId; }

    public String getSenderDisplayName() { return senderDisplayName; }
    public void setSenderDisplayName(String senderDisplayName) { this.senderDisplayName = senderDisplayName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public String getCipherKey() { return cipherKey; }
    public void setCipherKey(String cipherKey) { this.cipherKey = cipherKey; }

    // Helper methods
    public boolean isBlinkMessage() {
        return "blink".equals(rules);
    }

    public boolean isCipherMessage() {
        return "cipher".equals(rules);
    }

    public boolean hasExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }

    public boolean isSystemMessage() {
        return "system".equals(messageType);
    }
}