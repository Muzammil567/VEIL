package com.m3.veilchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.m3.veilchat.R;
import com.m3.veilchat.databinding.ActivityAppLockBinding;
import com.m3.veilchat.utils.SecurePrefsManager;

import java.util.concurrent.Executor;

public class AppLockActivity extends AppCompatActivity {
    private ActivityAppLockBinding binding;
    private SecurePrefsManager securePrefsManager;
    private boolean isSettingUp = false;
    private StringBuilder enteredPassword = new StringBuilder();
    private String confirmedPassword = "";
    private final int maxPasswordLength = 4;

    private BiometricPrompt biometricPrompt;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppLockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        securePrefsManager = new SecurePrefsManager(this);
        executor = ContextCompat.getMainExecutor(this);

        isSettingUp = getIntent().getBooleanExtra("setup", false);

        initializeUI();
        setupClickListeners();

        if (!isSettingUp && isBiometricAvailable() && securePrefsManager.isBiometricEnabled()) {
            showBiometricPrompt();
        }
    }

    private void initializeUI() {
        if (isSettingUp) {
            binding.tvTitle.setText("Set App Lock");
            binding.tvDescription.setText("Create a 4-digit PIN to secure your app");
            binding.btnBiometric.setVisibility(View.GONE);
            binding.btnForgot.setVisibility(View.GONE);
        } else {
            binding.tvTitle.setText("Unlock VEIL");
            binding.tvDescription.setText("Enter your PIN to continue");
            binding.btnBiometric.setVisibility(isBiometricAvailable() && securePrefsManager.isBiometricEnabled() ? View.VISIBLE : View.GONE);
        }

        updatePasswordDots();
    }

    private void setupClickListeners() {
        View.OnClickListener numpadClickListener = v -> {
            int id = v.getId();
            if (id == R.id.btn0) appendNumber("0");
            else if (id == R.id.btn1) appendNumber("1");
            else if (id == R.id.btn2) appendNumber("2");
            else if (id == R.id.btn3) appendNumber("3");
            else if (id == R.id.btn4) appendNumber("4");
            else if (id == R.id.btn5) appendNumber("5");
            else if (id == R.id.btn6) appendNumber("6");
            else if (id == R.id.btn7) appendNumber("7");
            else if (id == R.id.btn8) appendNumber("8");
            else if (id == R.id.btn9) appendNumber("9");
        };

        binding.btn0.setOnClickListener(numpadClickListener);
        binding.btn1.setOnClickListener(numpadClickListener);
        binding.btn2.setOnClickListener(numpadClickListener);
        binding.btn3.setOnClickListener(numpadClickListener);
        binding.btn4.setOnClickListener(numpadClickListener);
        binding.btn5.setOnClickListener(numpadClickListener);
        binding.btn6.setOnClickListener(numpadClickListener);
        binding.btn7.setOnClickListener(numpadClickListener);
        binding.btn8.setOnClickListener(numpadClickListener);
        binding.btn9.setOnClickListener(numpadClickListener);

        binding.btnDelete.setOnClickListener(v -> deleteLastNumber());
        binding.btnDelete.setOnLongClickListener(v -> {
            clearPassword();
            return true;
        });

        binding.btnBiometric.setOnClickListener(v -> showBiometricPrompt());
        binding.btnForgot.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void appendNumber(String number) {
        if (enteredPassword.length() < maxPasswordLength) {
            enteredPassword.append(number);
            updatePasswordDots();

            if (enteredPassword.length() == maxPasswordLength) {
                if (isSettingUp) {
                    handleSetupPassword();
                } else {
                    handleUnlock();
                }
            }
        }
    }

    private void deleteLastNumber() {
        if (enteredPassword.length() > 0) {
            enteredPassword.deleteCharAt(enteredPassword.length() - 1);
            updatePasswordDots();
        }
    }

    private void clearPassword() {
        enteredPassword.setLength(0);
        updatePasswordDots();
    }

    private void updatePasswordDots() {
        int length = enteredPassword.length();
        binding.dot1.setImageResource(length >= 1 ? R.drawable.dot_filled : R.drawable.dot_empty);
        binding.dot2.setImageResource(length >= 2 ? R.drawable.dot_filled : R.drawable.dot_empty);
        binding.dot3.setImageResource(length >= 3 ? R.drawable.dot_filled : R.drawable.dot_empty);
        binding.dot4.setImageResource(length >= 4 ? R.drawable.dot_filled : R.drawable.dot_empty);
    }

    private void handleSetupPassword() {
        if (confirmedPassword.isEmpty()) {
            confirmedPassword = enteredPassword.toString();
            clearPassword();
            binding.tvTitle.setText("Confirm PIN");
            binding.tvDescription.setText("Re-enter your PIN to confirm");
        } else {
            if (enteredPassword.toString().equals(confirmedPassword)) {
                saveAppLockPassword(confirmedPassword);
                Toast.makeText(this, "App lock enabled", Toast.LENGTH_SHORT).show();
                if (isBiometricAvailable()) {
                    showBiometricSetupDialog();
                } else {
                    finish();
                }
            } else {
                handleWrongPassword("PINs don't match. Try again.");
                confirmedPassword = "";
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    binding.tvTitle.setText("Set App Lock");
                    binding.tvDescription.setText("Create a 4-digit PIN to secure your app");
                }, 2000);
            }
        }
    }

    private void handleUnlock() {
        if (securePrefsManager.validateAppLockPassword(enteredPassword.toString())) {
            unlockApp();
        } else {
            handleWrongPassword("Wrong PIN. Try again.");
        }
    }

    private void handleWrongPassword(String message) {
        binding.passwordContainer.animate().translationX(20).withEndAction(() ->
                binding.passwordContainer.animate().translationX(-20).withEndAction(() ->
                        binding.passwordContainer.animate().translationX(0).setDuration(50).start()
                ).setDuration(100).start()
        ).setDuration(50).start();

        binding.tvDescription.setText(message);
        clearPassword();
    }

    private void saveAppLockPassword(String password) {
        securePrefsManager.setAppLockPassword(password);
        securePrefsManager.setAppLockEnabled(true);
    }

    private void unlockApp() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock VEIL")
                .setSubtitle("Use your fingerprint to unlock")
                .setNegativeButtonText("Use PIN")
                .build();

        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                    Toast.makeText(AppLockActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                unlockApp();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }

    private void showBiometricSetupDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Enable Biometric Unlock")
                .setMessage("Do you want to enable fingerprint or face unlock for faster access?")
                .setPositiveButton("Enable", (dialog, which) -> {
                    securePrefsManager.setBiometricEnabled(true);
                    Toast.makeText(this, "Biometric unlock enabled", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Skip", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showForgotPasswordDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Forgot PIN?")
                .setMessage("This will reset your app lock and you'll need to set it up again. Continue?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    securePrefsManager.clearAppLock();
                    Toast.makeText(this, "App lock reset", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, AppLockActivity.class);
                    intent.putExtra("setup", true);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    @Override
    public void onBackPressed() {
        if (!isSettingUp) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }
}
