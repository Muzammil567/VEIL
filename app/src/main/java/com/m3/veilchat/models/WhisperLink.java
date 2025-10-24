package com.m3.veilchat.models;

public class WhisperLink {
    private String linkId;
    private String creatorId;
    private String roomId;
    private int maxUses;
    private int timesUsed;
    private long createdAt;
    private long expiresAt;
    private boolean isActive;
    private boolean selfDestruct;

    public WhisperLink() {
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.selfDestruct = true;
    }

    public WhisperLink(String linkId, String creatorId, int maxUses, long expiresAt) {
        this();
        this.linkId = linkId;
        this.creatorId = creatorId;
        this.maxUses = maxUses;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public String getLinkId() { return linkId; }
    public void setLinkId(String linkId) { this.linkId = linkId; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }

    public int getTimesUsed() { return timesUsed; }
    public void setTimesUsed(int timesUsed) { this.timesUsed = timesUsed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isSelfDestruct() { return selfDestruct; }
    public void setSelfDestruct(boolean selfDestruct) { this.selfDestruct = selfDestruct; }

    // Helper methods
    public boolean isValid() {
        return isActive &&
                System.currentTimeMillis() < expiresAt &&
                timesUsed < maxUses;
    }

    public boolean hasExpired() {
        return !isValid();
    }

    public String getShareableUrl() {
        return "https://veil.chat/whisper/" + linkId;
    }
}