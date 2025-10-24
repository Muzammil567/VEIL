package com.m3.veilchat.utils;

import android.app.Activity;
import android.view.WindowManager;

public class ScreenshotProtectionHelper {

    public static void enableScreenshotProtection(Activity activity) {
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );
    }

    public static void disableScreenshotProtection(Activity activity) {
        activity.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_SECURE
        );
    }

    // Blur effect for attempted screenshots (conceptual)
    public static void enableBlurProtection(Activity activity) {
        // This would require custom view overlays that detect screenshot attempts
        // and show a blur effect. This is more complex and would need root access
        // or advanced permissions for real implementation.
    }
}