package com.m3.veilchat.models;

import java.util.ArrayList;
import java.util.List;

public class BlinkStory {
    private String storyId;
    private String creatorPersonaId;
    private String creatorDisplayName;
    private String content;
    private long createdAt;
    private long expiresAt;
    private List<String> viewedByUserIds;
    private int viewCount;
    private String moodTag;

    public BlinkStory() {}

    public BlinkStory(String creatorPersonaId, String creatorDisplayName, String content) {
        this.creatorPersonaId = creatorPersonaId;
        this.creatorDisplayName = creatorDisplayName;
        this.content = content;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 hours
        this.viewCount = 0;
    }

    // Getters and Setters
    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public String getCreatorPersonaId() { return creatorPersonaId; }
    public void setCreatorPersonaId(String creatorPersonaId) { this.creatorPersonaId = creatorPersonaId; }

    public String getCreatorDisplayName() { return creatorDisplayName; }
    public void setCreatorDisplayName(String creatorDisplayName) { this.creatorDisplayName = creatorDisplayName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public List<String> getViewedByUserIds() { return viewedByUserIds; }
    public void setViewedByUserIds(List<String> viewedByUserIds) { this.viewedByUserIds = viewedByUserIds; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public String getMoodTag() { return moodTag; }
    public void setMoodTag(String moodTag) { this.moodTag = moodTag; }

    public boolean hasExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public boolean hasUserViewed(String userId) {
        return viewedByUserIds != null && viewedByUserIds.contains(userId);
    }

    public void markAsViewed(String userId) {
        if (viewedByUserIds == null) {
            viewedByUserIds = new ArrayList<>();
        }
        if (!viewedByUserIds.contains(userId)) {
            viewedByUserIds.add(userId);
            viewCount++;
        }
    }
}