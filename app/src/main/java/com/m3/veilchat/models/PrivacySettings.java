package com.m3.veilchat.models;

public class PrivacySettings {
    private boolean silentMode = false;
    private boolean readReceipts = true;
    private boolean typingIndicators = true;
    private boolean onlineStatus = true;
    private boolean vanishCalls = true;
    private boolean screenshotProtection = true;
    private boolean secretInboxEnabled = false;
    private String secretInboxPassword;

    // Getters and Setters
    public boolean isSilentMode() { return silentMode; }
    public void setSilentMode(boolean silentMode) { this.silentMode = silentMode; }

    public boolean isReadReceipts() { return readReceipts; }
    public void setReadReceipts(boolean readReceipts) { this.readReceipts = readReceipts; }

    public boolean isTypingIndicators() { return typingIndicators; }
    public void setTypingIndicators(boolean typingIndicators) { this.typingIndicators = typingIndicators; }

    public boolean isOnlineStatus() { return onlineStatus; }
    public void setOnlineStatus(boolean onlineStatus) { this.onlineStatus = onlineStatus; }

    public boolean isVanishCalls() { return vanishCalls; }
    public void setVanishCalls(boolean vanishCalls) { this.vanishCalls = vanishCalls; }

    public boolean isScreenshotProtection() { return screenshotProtection; }
    public void setScreenshotProtection(boolean screenshotProtection) { this.screenshotProtection = screenshotProtection; }

    public boolean isSecretInboxEnabled() { return secretInboxEnabled; }
    public void setSecretInboxEnabled(boolean secretInboxEnabled) { this.secretInboxEnabled = secretInboxEnabled; }

    public String getSecretInboxPassword() { return secretInboxPassword; }
    public void setSecretInboxPassword(String secretInboxPassword) { this.secretInboxPassword = secretInboxPassword; }
}