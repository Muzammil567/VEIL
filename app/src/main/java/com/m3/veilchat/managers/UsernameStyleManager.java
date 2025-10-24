package com.m3.veilchat.managers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import com.m3.veilchat.utils.SecurePrefsManager;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Random;

public class UsernameStyleManager {
    private static final String TAG = "UsernameStyleManager";
    private SecurePrefsManager securePrefsManager;
    private Random random;

    // Predefined color schemes
    private final String[] colorSchemes = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#DDA0DD", "#98D8C8", "#F7DC6F",
            "#BB8FCE", "#85C1E9", "#F8C471", "#82E0AA", "#F1948A", "#85C1E9", "#D7BDE2", "#F9E79F"
    };

    // Font styles (would need custom fonts in production)
    private final String[] fontStyles = {"normal", "bold", "italic"};

    public UsernameStyleManager(Context context) {
        this.securePrefsManager = new SecurePrefsManager(context);
        this.random = new Random();
    }

    // Generate daily style based on user ID and current date
    public UsernameStyle getTodaysStyle(String userId) {
        String styleKey = getDailyStyleKey(userId);

        // Check if we already generated today's style
        UsernameStyle cachedStyle = getCachedStyle(styleKey);
        if (cachedStyle != null) {
            return cachedStyle;
        }

        // Generate new style for today
        UsernameStyle newStyle = generateStyle(userId);
        cacheStyle(styleKey, newStyle);

        return newStyle;
    }

    private UsernameStyle generateStyle(String userId) {
        long dailySeed = getDailySeed(userId);
        Random dailyRandom = new Random(dailySeed);

        UsernameStyle style = new UsernameStyle();

        // Color based on daily seed
        int colorIndex = (int) (dailySeed % colorSchemes.length);
        style.setTextColor(colorSchemes[colorIndex]);

        // Font style
        int fontIndex = dailyRandom.nextInt(fontStyles.length);
        style.setFontStyle(fontStyles[fontIndex]);

        // Optional: text shadow
        style.setHasShadow(dailyRandom.nextBoolean());

        // Optional: gradient effect occasionally
        style.setHasGradient(dailyRandom.nextDouble() < 0.2); // 20% chance

        if (style.hasGradient()) {
            int secondColorIndex = (colorIndex + 1 + dailyRandom.nextInt(colorSchemes.length - 1)) % colorSchemes.length;
            style.setSecondaryColor(colorSchemes[secondColorIndex]);
        }

        return style;
    }

    private long getDailySeed(String userId) {
        try {
            // Combine user ID with current date to get daily-changing seed
            String dateString = LocalDate.now().toString();
            String seedString = userId + dateString;

            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(seedString.getBytes());

            // Convert first 8 bytes to long
            long seed = 0;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xFF);
            }

            return seed;
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate daily seed", e);
            return System.currentTimeMillis() / (24 * 60 * 60 * 1000); // Fallback: daily changing
        }
    }

    private String getDailyStyleKey(String userId) {
        String today = java.time.LocalDate.now().toString();
        return "username_style_" + userId + "_" + today;
    }

    private UsernameStyle getCachedStyle(String key) {
        // Implement caching in SharedPreferences
        String cached = securePrefsManager.getCachedStyle(key);
        if (cached != null) {
            return UsernameStyle.fromJson(cached);
        }
        return null;
    }

    private void cacheStyle(String key, UsernameStyle style) {
        securePrefsManager.cacheStyle(key, style.toJson());
    }

    public static class UsernameStyle {
        private String textColor;
        private String secondaryColor;
        private String fontStyle;
        private boolean hasShadow;
        private boolean hasGradient;

        // Getters and Setters
        public String getTextColor() { return textColor; }
        public void setTextColor(String textColor) { this.textColor = textColor; }

        public String getSecondaryColor() { return secondaryColor; }
        public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

        public String getFontStyle() { return fontStyle; }
        public void setFontStyle(String fontStyle) { this.fontStyle = fontStyle; }

        public boolean hasShadow() { return hasShadow; }
        public void setHasShadow(boolean hasShadow) { this.hasShadow = hasShadow; }

        public boolean hasGradient() { return hasGradient; }
        public void setHasGradient(boolean hasGradient) { this.hasGradient = hasGradient; }

        public String toJson() {
            // Simple JSON serialization
            return String.format("{\"color\":\"%s\",\"font\":\"%s\",\"shadow\":%b,\"gradient\":%b}",
                    textColor, fontStyle, hasShadow, hasGradient);
        }

        public static UsernameStyle fromJson(String json) {
            // Simple JSON parsing
            UsernameStyle style = new UsernameStyle();
            try {
                if (json.contains("\"color\":")) {
                    String color = json.split("\"color\":\"")[1].split("\"")[0];
                    style.setTextColor(color);
                }
                if (json.contains("\"font\":")) {
                    String font = json.split("\"font\":\"")[1].split("\"")[0];
                    style.setFontStyle(font);
                }
                if (json.contains("\"shadow\":")) {
                    String shadow = json.split("\"shadow\":")[1].split(",")[0];
                    style.setHasShadow(Boolean.parseBoolean(shadow));
                }
                if (json.contains("\"gradient\":")) {
                    String gradient = json.split("\"gradient\":")[1].split("}")[0];
                    style.setHasGradient(Boolean.parseBoolean(gradient));
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse style JSON", e);
            }
            return style;
        }
    }
}