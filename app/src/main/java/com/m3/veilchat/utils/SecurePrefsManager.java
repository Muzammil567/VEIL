package com.m3.veilchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

public class SecurePrefsManager {
    private static final String TAG = "SecurePrefsManager";
    private static final String PREFS_NAME = "veil_secure_prefs";

    private SharedPreferences secureSharedPreferences;
    private SharedPreferences regularSharedPreferences;

    public SecurePrefsManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            secureSharedPreferences = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular shared preferences (less secure)
            secureSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        // Regular prefs for non-sensitive data
        regularSharedPreferences = context.getSharedPreferences("veil_regular_prefs", Context.MODE_PRIVATE);
    }

    // 🔐 App Lock Methods

    public void setAppLockRequired(boolean required) {
        regularSharedPreferences.edit().putBoolean("app_lock_required", required).apply();
    }

    public boolean isAppLockRequired() {
        return regularSharedPreferences.getBoolean("app_lock_required", false);
    }



    // Biometric methods
    public void setBiometricEnabled(boolean enabled) {
        secureSharedPreferences.edit().putBoolean("biometric_enabled", enabled).apply();
    }

    public boolean isBiometricEnabled() {
        return secureSharedPreferences.getBoolean("biometric_enabled", false);
    }

    // App lock methods
    public void setAppLockPassword(String password) {
        String hash = EncryptionUtils.hashPassword(password);
        if (hash != null) {
            secureSharedPreferences.edit().putString("app_lock_hash", hash).apply();
        }
    }

    public boolean validateAppLockPassword(String password) {
        String storedHash = secureSharedPreferences.getString("app_lock_hash", null);
        return storedHash != null && EncryptionUtils.validatePassword(password, storedHash);
    }

    public boolean isAppLockEnabled() {
        return secureSharedPreferences.contains("app_lock_hash");
    }

    public void clearAppLock() {
        secureSharedPreferences.edit()
                .remove("app_lock_hash")
                .remove("biometric_enabled")
                .apply();
    }


    // 🔑 Encryption Key Management
    public void storeRoomKey(String roomId, SecretKey secretKey) {
        String keyString = EncryptionUtils.keyToString(secretKey);
        secureSharedPreferences.edit().putString("room_key_" + roomId, keyString).apply();
    }

    public SecretKey getRoomKey(String roomId) {
        String keyString = secureSharedPreferences.getString("room_key_" + roomId, null);
        if (keyString != null) {
            return EncryptionUtils.stringToKey(keyString);
        }
        return null;
    }

    // 🔐 Cipher Key Management
    public void storeCipherKey(String chatId, String cipherKey) {
        secureSharedPreferences.edit().putString("cipher_key_" + chatId, cipherKey).apply();
    }

    public String getCipherKey(String chatId) {
        return secureSharedPreferences.getString("cipher_key_" + chatId, null);
    }

    // 📧 Secret Inbox Methods
    public void setSecretInboxPassword(String password) {
        String hash = EncryptionUtils.hashPassword(password);
        if (hash != null) {
            secureSharedPreferences.edit().putString("secret_inbox_hash", hash).apply();
        }
    }

    public boolean validateSecretInboxPassword(String password) {
        String storedHash = secureSharedPreferences.getString("secret_inbox_hash", null);
        return storedHash != null && EncryptionUtils.validatePassword(password, storedHash);
    }

    public boolean isSecretInboxEnabled() {
        return secureSharedPreferences.getBoolean("secret_inbox_enabled", false);
    }

    public void setSecretInboxEnabled(boolean enabled) {
        secureSharedPreferences.edit().putBoolean("secret_inbox_enabled", enabled).apply();
    }

    // 🔒 Privacy Settings
    public void setSilentMode(boolean enabled) {
        regularSharedPreferences.edit().putBoolean("silent_mode", enabled).apply();
    }

    public boolean isSilentModeEnabled() {
        return regularSharedPreferences.getBoolean("silent_mode", false);
    }

    public void setReadReceiptsEnabled(boolean enabled) {
        regularSharedPreferences.edit().putBoolean("read_receipts", enabled).apply();
    }

    public boolean areReadReceiptsEnabled() {
        return regularSharedPreferences.getBoolean("read_receipts", true);
    }

    public void setTypingIndicatorsEnabled(boolean enabled) {
        regularSharedPreferences.edit().putBoolean("typing_indicators", enabled).apply();
    }

    public boolean areTypingIndicatorsEnabled() {
        return regularSharedPreferences.getBoolean("typing_indicators", true);
    }

    public void setVanishCallsEnabled(boolean enabled) {
        regularSharedPreferences.edit().putBoolean("vanish_calls", enabled).apply();
    }

    public boolean isVanishCallsEnabled() {
        return regularSharedPreferences.getBoolean("vanish_calls", true);
    }

    public void setScreenshotProtectionEnabled(boolean enabled) {
        regularSharedPreferences.edit().putBoolean("screenshot_protection", enabled).apply();
    }

    public boolean isScreenshotProtectionEnabled() {
        return regularSharedPreferences.getBoolean("screenshot_protection", true);
    }

    // 👤 User Preferences
    public void setCurrentPersonaId(String personaId) {
        regularSharedPreferences.edit().putString("current_persona_id", personaId).apply();
    }

    public String getCurrentPersonaId() {
        return regularSharedPreferences.getString("current_persona_id", null);
    }

    public void setLastRoom(String roomId) {
        regularSharedPreferences.edit().putString("last_room", roomId).apply();
    }

    public String getLastRoom() {
        return regularSharedPreferences.getString("last_room", null);
    }

    // 🎨 Theme & Appearance
    public void setDarkModeEnabled(boolean enabled) {
        regularSharedPreferences.edit().putBoolean("dark_mode", enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return regularSharedPreferences.getBoolean("dark_mode", true);
    }

    public void setVibeScore(String userId, int score) {
        regularSharedPreferences.edit().putInt("vibe_score_" + userId, score).apply();
    }

    public int getVibeScore(String userId) {
        return regularSharedPreferences.getInt("vibe_score_" + userId, 0);
    }

    // 🔄 Session Management
    public void setUserLoggedIn(boolean loggedIn) {
        regularSharedPreferences.edit().putBoolean("user_logged_in", loggedIn).apply();
    }

    public boolean isUserLoggedIn() {
        return regularSharedPreferences.getBoolean("user_logged_in", false);
    }

    public void setLastActive(long timestamp) {
        regularSharedPreferences.edit().putLong("last_active", timestamp).apply();
    }

    public long getLastActive() {
        return regularSharedPreferences.getLong("last_active", 0);
    }

    // 🗑️ Data Management
    public void clearAllData() {
        // Clear secure preferences
        secureSharedPreferences.edit().clear().apply();

        // Clear regular preferences (except some settings)
        SharedPreferences.Editor editor = regularSharedPreferences.edit();

        // Keep theme preference
        boolean darkMode = isDarkModeEnabled();

        editor.clear();

        // Restore theme preference
        if (darkMode) {
            editor.putBoolean("dark_mode", true);
        }

        editor.apply();
    }

    public void clearSessionData() {
        // Clear session-specific data but keep settings
        regularSharedPreferences.edit()
                .remove("user_logged_in")
                .remove("last_active")
                .remove("current_persona_id")
                .remove("last_room")
                .remove("temp_keys")
                .remove("session_tokens")
                .apply();
    }

    public void clearEncryptionKeys() {
        // Remove all encryption keys
        SharedPreferences.Editor editor = secureSharedPreferences.edit();

        // Remove room keys
        for (String key : secureSharedPreferences.getAll().keySet()) {
            if (key.startsWith("room_key_") || key.startsWith("cipher_key_")) {
                editor.remove(key);
            }
        }

        editor.apply();
    }

    // 📊 Analytics & Usage
    public void incrementMessageCount() {
        int count = regularSharedPreferences.getInt("total_messages", 0);
        regularSharedPreferences.edit().putInt("total_messages", count + 1).apply();
    }

    public int getTotalMessages() {
        return regularSharedPreferences.getInt("total_messages", 0);
    }

    public void incrementCallCount() {
        int count = regularSharedPreferences.getInt("total_calls", 0);
        regularSharedPreferences.edit().putInt("total_calls", count + 1).apply();
    }

    public int getTotalCalls() {
        return regularSharedPreferences.getInt("total_calls", 0);
    }

    public void setFirstLaunch(boolean firstLaunch) {
        regularSharedPreferences.edit().putBoolean("first_launch", firstLaunch).apply();
    }

    public boolean isFirstLaunch() {
        return regularSharedPreferences.getBoolean("first_launch", true);
    }

    // 🔍 Debug & Development
    public void setDebugMode(boolean enabled) {
        regularSharedPreferences.edit().putBoolean("debug_mode", enabled).apply();
    }

    public boolean isDebugMode() {
        return regularSharedPreferences.getBoolean("debug_mode", false);
    }

    public void setLastError(String error) {
        regularSharedPreferences.edit().putString("last_error", error).apply();
    }

    public String getLastError() {
        return regularSharedPreferences.getString("last_error", null);
    }
    // Cache for daily username styles
    public void cacheStyle(String key, String styleJson) {
        secureSharedPreferences.edit().putString("style_" + key, styleJson).apply();
    }

    public String getCachedStyle(String key) {
        return secureSharedPreferences.getString("style_" + key, null);
    }

    // Blink stories settings
    public void setStoriesEnabled(boolean enabled) {
        secureSharedPreferences.edit().putBoolean("stories_enabled", enabled).apply();
    }

    public boolean areStoriesEnabled() {
        return secureSharedPreferences.getBoolean("stories_enabled", true);
    }

    // Whisper link history
    public void addWhisperLinkToHistory(String linkCode) {
        Set<String> history = getWhisperLinkHistory();
        history.add(linkCode);
        secureSharedPreferences.edit().putStringSet("whisper_link_history", history).apply();
    }

    public Set<String> getWhisperLinkHistory() {
        return secureSharedPreferences.getStringSet("whisper_link_history", new HashSet<>());
    }
}