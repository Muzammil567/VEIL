package com.m3.veilchat.models;

public class Mood {
    private String moodId;
    private String emoji;
    private String displayName;
    private String description;

    public Mood() {}

    public Mood(String emoji, String displayName, String description) {
        this.emoji = emoji;
        this.displayName = displayName;
        this.description = description;
    }

    // Getters and Setters
    public String getMoodId() { return moodId; }
    public void setMoodId(String moodId) { this.moodId = moodId; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}