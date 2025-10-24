package com.m3.veilchat.models;

public class Persona {
    private String personaId;
    private String name;
    private String displayName;
    private String mood; // e.g., "lonely", "excited", "bored"
    private String interestTag; // e.g., "#music", "#tech"
    private String vibeStyle; // Visual theme for this persona
    private String avatarUrl;
    private long createdAt;
    private boolean isActive;

    // Constructors
    public Persona() {}

    public Persona(String name, String displayName, String mood, String interestTag) {
        this.name = name;
        this.displayName = displayName;
        this.mood = mood;
        this.interestTag = interestTag;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    // Getters and Setters
    public String getPersonaId() { return personaId; }
    public void setPersonaId(String personaId) { this.personaId = personaId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getInterestTag() { return interestTag; }
    public void setInterestTag(String interestTag) { this.interestTag = interestTag; }

    public String getVibeStyle() { return vibeStyle; }
    public void setVibeStyle(String vibeStyle) { this.vibeStyle = vibeStyle; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}