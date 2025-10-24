package com.m3.veilchat.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class User {
    private String userId;
    private String anonymousId;
    private String email;
    private String username;
    private long createdAt;
    private boolean isEmailVerified;
    private List<Persona> personas;
    private String currentPersonaId;
    private Map<String, Integer> vibeScores; // userId -> score
    private Map<String, String> localNicknames; // userId -> nickname
    private PrivacySettings privacySettings;
    private List<TrustBadge> trustBadges;
    private int totalConfessions;
    private long lastActive;
    private String currentMood;

    private String currentAlias;
    private long lastAliasChange;
    private List<String> previousAliases;

    // Constructors
    public User() {
        this.personas = new ArrayList<>();
        this.vibeScores = new HashMap<>();
        this.localNicknames = new HashMap<>();
        this.privacySettings = new PrivacySettings();
        this.trustBadges = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.lastActive = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAnonymousId() { return anonymousId; }
    public void setAnonymousId(String anonymousId) { this.anonymousId = anonymousId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isEmailVerified() { return isEmailVerified; }
    public void setEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }

    public List<Persona> getPersonas() { return personas; }
    public void setPersonas(List<Persona> personas) { this.personas = personas; }

    public String getCurrentPersonaId() { return currentPersonaId; }
    public void setCurrentPersonaId(String currentPersonaId) { this.currentPersonaId = currentPersonaId; }

    public Map<String, Integer> getVibeScores() { return vibeScores; }
    public void setVibeScores(Map<String, Integer> vibeScores) { this.vibeScores = vibeScores; }

    public Map<String, String> getLocalNicknames() { return localNicknames; }
    public void setLocalNicknames(Map<String, String> localNicknames) { this.localNicknames = localNicknames; }

    public PrivacySettings getPrivacySettings() { return privacySettings; }
    public void setPrivacySettings(PrivacySettings privacySettings) { this.privacySettings = privacySettings; }

    public List<TrustBadge> getTrustBadges() { return trustBadges; }
    public void setTrustBadges(List<TrustBadge> trustBadges) { this.trustBadges = trustBadges; }

    public int getTotalConfessions() { return totalConfessions; }
    public void setTotalConfessions(int totalConfessions) { this.totalConfessions = totalConfessions; }

    public long getLastActive() { return lastActive; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }

    public String getCurrentMood() { return currentMood; }
    public void setCurrentMood(String currentMood) { this.currentMood = currentMood; }

    // Helper methods
    public Persona getCurrentPersona() {
        if (currentPersonaId == null || personas == null) return null;
        for (Persona persona : personas) {
            if (persona.getPersonaId().equals(currentPersonaId)) {
                return persona;
            }
        }
        return null;
    }

    public void addVibeScore(String userId, int score) {
        vibeScores.put(userId, score);
    }

    public int getVibeScoreWith(String userId) {
        return vibeScores.getOrDefault(userId, 0);
    }

    public void addLocalNickname(String userId, String nickname) {
        localNicknames.put(userId, nickname);
    }

    public String getLocalNickname(String userId) {
        return localNicknames.get(userId);
    }

    public boolean canChangeAlias() {
        long oneMonthInMillis = 30L * 24 * 60 * 60 * 1000;
        return System.currentTimeMillis() - lastAliasChange >= oneMonthInMillis;
    }

    public void changeAlias(String newAlias) {
        if (previousAliases == null) {
            previousAliases = new ArrayList<>();
        }

        // Add current alias to previous aliases
        if (currentAlias != null) {
            previousAliases.add(currentAlias);
        }

        this.currentAlias = newAlias;
        this.lastAliasChange = System.currentTimeMillis();
    }

    // Group chat masking
    public String getMaskedAlias(String roomId) {
        // Generate consistent but masked alias per room
        long seed = roomId.hashCode() + currentAlias.hashCode();
        Random random = new Random(seed);

        String[] animals = {"🐺 Wolf", "🦊 Fox", "🐱 Cat", "🐯 Tiger", "🦁 Lion", "🐻 Bear", "🐼 Panda", "🐨 Koala"};
        String[] emojis = {"👤", "🎭", "🕵️", "👻", "💀", "🤖", "👽", "🦹"};

        if (random.nextBoolean()) {
            return animals[random.nextInt(animals.length)];
        } else {
            return emojis[random.nextInt(emojis.length)];
        }
    }
}