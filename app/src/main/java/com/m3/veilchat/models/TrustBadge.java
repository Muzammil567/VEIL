package com.m3.veilchat.models;

public class TrustBadge {
    private String badgeId;
    private String badgeType; // "mutual_trust", "vibe_master", "confession_king", etc.
    private String fromUserId;
    private long awardedAt;
    private String description;

    public TrustBadge() {}

    public TrustBadge(String badgeType, String fromUserId, long awardedAt) {
        this.badgeType = badgeType;
        this.fromUserId = fromUserId;
        this.awardedAt = awardedAt;
    }

    // Getters and Setters
    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }

    public String getBadgeType() { return badgeType; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public long getAwardedAt() { return awardedAt; }
    public void setAwardedAt(long awardedAt) { this.awardedAt = awardedAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}