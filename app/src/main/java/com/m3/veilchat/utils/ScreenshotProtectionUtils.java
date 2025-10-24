package com.m3.veilchat.utils;

import android.app.Activity;
import android.view.WindowManager;
import android.util.Log;

public class ScreenshotProtectionUtils {
    private static final String TAG = "ScreenshotProtection";

    public static void enableScreenshotProtection(Activity activity) {
        try {
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
            );
            Log.d(TAG, "Screenshot protection enabled");
        } catch (Exception e) {
            Log.e(TAG, "Failed to enable screenshot protection", e);
        }
    }

    public static void disableScreenshotProtection(Activity activity) {
        try {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            Log.d(TAG, "Screenshot protection disabled");
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable screenshot protection", e);
        }
    }

    // Blur effect for attempted screenshots (conceptual)
    public static void setupScreenshotBlur(Activity activity) {
        // This would require root access or custom ROM in production
        // For now, we use FLAG_SECURE which prevents screenshots entirely
        enableScreenshotProtection(activity);
    }
}