package com.m3.veilchat.fragments.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.m3.veilchat.databinding.DialogPrivacySettingsBinding;
import com.m3.veilchat.models.PrivacySettings;
import com.m3.veilchat.viewmodels.UserViewModel;
import androidx.lifecycle.ViewModelProvider;

public class PrivacySettingsDialog extends DialogFragment {
    private DialogPrivacySettingsBinding binding;
    private UserViewModel userViewModel;
    private PrivacySettings currentSettings;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        binding = DialogPrivacySettingsBinding.inflate(LayoutInflater.from(requireContext()));

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        setupObservers();
        setupClickListeners();

        builder.setView(binding.getRoot())
                .setTitle("Privacy Settings")
                .setPositiveButton("Save", (dialog, which) -> saveSettings())
                .setNegativeButton("Cancel", null);

        return builder.create();
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null && user.getPrivacySettings() != null) {
                currentSettings = user.getPrivacySettings();
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (currentSettings == null) return;

        binding.switchSilentMode.setChecked(currentSettings.isSilentMode());
        binding.switchReadReceipts.setChecked(currentSettings.isReadReceipts());
        binding.switchTypingIndicators.setChecked(currentSettings.isTypingIndicators());
        binding.switchOnlineStatus.setChecked(currentSettings.isOnlineStatus());
        binding.switchVanishCalls.setChecked(currentSettings.isVanishCalls());
        binding.switchScreenshotProtection.setChecked(currentSettings.isScreenshotProtection());
        binding.switchSecretInbox.setChecked(currentSettings.isSecretInboxEnabled());
    }

    private void setupClickListeners() {
        binding.switchSilentMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null) {
                currentSettings.setSilentMode(isChecked);
            }
        });

        binding.switchReadReceipts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null) {
                currentSettings.setReadReceipts(isChecked);
            }
        });

        binding.switchTypingIndicators.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null) {
                currentSettings.setTypingIndicators(isChecked);
            }
        });

        binding.switchOnlineStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null) {
                currentSettings.setOnlineStatus(isChecked);
            }
        });

        binding.switchVanishCalls.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null) {
                currentSettings.setVanishCalls(isChecked);
            }
        });

        binding.switchScreenshotProtection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null) {
                currentSettings.setScreenshotProtection(isChecked);
            }
        });

        binding.switchSecretInbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null) {
                currentSettings.setSecretInboxEnabled(isChecked);
            }
        });
    }

    private void saveSettings() {
        if (currentSettings != null) {
            userViewModel.updatePrivacySettings(currentSettings);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}