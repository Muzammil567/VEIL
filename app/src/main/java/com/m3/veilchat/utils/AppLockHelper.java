package com.m3.veilchat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.m3.veilchat.activities.AppLockActivity;

public class AppLockHelper {
    private final SecurePrefsManager securePrefsManager;
    private final Context context;

    public AppLockHelper(Context context) {
        this.context = context;
        this.securePrefsManager = new SecurePrefsManager(context);
    }

    public void checkAppLock(Activity activity) {
        if (securePrefsManager.isAppLockEnabled()) {
            // Show app lock screen only if we're not already on it
            if (!(activity instanceof AppLockActivity)) {
                Intent intent = new Intent(activity, AppLockActivity.class);
                intent.putExtra("setup", false);
                activity.startActivity(intent);
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }
    }

    public void setupAppLock(Activity activity) {
        Intent intent = new Intent(activity, AppLockActivity.class);
        intent.putExtra("setup", true);
        activity.startActivity(intent);
    }

    public boolean isAppLockEnabled() {
        return securePrefsManager.isAppLockEnabled();
    }

    public boolean isBiometricEnabled() {
        return securePrefsManager.isBiometricEnabled();
    }

    public void enableAppLock(String password) {
        securePrefsManager.setAppLockPassword(password);
    }

    public boolean validateAppLock(String password) {
        return securePrefsManager.validateAppLockPassword(password);
    }

    public void disableAppLock() {
        securePrefsManager.clearAppLock();
    }

    public void enableBiometric() {
        securePrefsManager.setBiometricEnabled(true);
    }

    public void lockApp(Activity activity) {
        // Clear any session data and show lock screen
        securePrefsManager.clearSessionData();

        Intent intent = new Intent(activity, AppLockActivity.class);
        intent.putExtra("setup", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public void showAppLockSettings(Activity activity) {
        // Show dialog with app lock options
        String[] options;
        if (isAppLockEnabled()) {
            options = new String[]{
                    "Change PIN",
                    isBiometricEnabled() ? "Disable Biometric" : "Enable Biometric",
                    "Disable App Lock"
            };
        } else {
            options = new String[]{"Enable App Lock"};
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        builder.setTitle("App Lock Settings");
        builder.setItems(options, (dialog, which) -> {
            if (isAppLockEnabled()) {
                switch (which) {
                    case 0: // Change PIN
                        setupAppLock(activity);
                        break;
                    case 1: // Toggle Biometric
                        if (isBiometricEnabled()) {
                            securePrefsManager.setBiometricEnabled(false);
                            android.widget.Toast.makeText(activity, "Biometric disabled", android.widget.Toast.LENGTH_SHORT).show();
                        } else {
                            securePrefsManager.setBiometricEnabled(true);
                            android.widget.Toast.makeText(activity, "Biometric enabled", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2: // Disable App Lock
                        disableAppLock();
                        android.widget.Toast.makeText(activity, "App lock disabled", android.widget.Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                setupAppLock(activity);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
