package com.m3.veilchat.models;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
    private String roomId;
    private String name;
    private String description;
    private String createdBy;
    private long createdAt;
    private String roomType; // "public", "private", "ephemeral"
    private List<String> participantIds;
    private List<String> adminIds;
    private String moodTag;
    private String interestTag;
    private boolean isActive;
    private long expiresAt; // For ephemeral rooms
    private int maxParticipants;
    private String rules; // "ephemeral", "blink", "cipher", etc.

    // Constructors
    public ChatRoom() {
        this.participantIds = new ArrayList<>();
        this.adminIds = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.maxParticipants = 50;
    }

    public ChatRoom(String name, String description, String createdBy, String roomType) {
        this();
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.roomType = roomType;
    }

    // Getters and Setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    public List<String> getAdminIds() { return adminIds; }
    public void setAdminIds(List<String> adminIds) { this.adminIds = adminIds; }

    public String getMoodTag() { return moodTag; }
    public void setMoodTag(String moodTag) { this.moodTag = moodTag; }

    public String getInterestTag() { return interestTag; }
    public void setInterestTag(String interestTag) { this.interestTag = interestTag; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    // Helper methods
    public void addParticipant(String userId) {
        if (!participantIds.contains(userId)) {
            participantIds.add(userId);
        }
    }

    public void removeParticipant(String userId) {
        participantIds.remove(userId);
    }

    public boolean isEphemeral() {
        return "ephemeral".equals(roomType);
    }

    public boolean hasExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }
}