package com.m3.veilchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.m3.veilchat.R;
import com.m3.veilchat.databinding.ActivityWelcomeBinding;
import com.m3.veilchat.utils.ShareUtils;
import com.m3.veilchat.viewmodels.AuthViewModel;

public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnContinueAnonymously.setEnabled(!isLoading);
        });

        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        authViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                binding.tvError.setText(errorMessage);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnContinueAnonymously.setOnClickListener(v -> {
            authViewModel.signInAnonymously();
        });

        // Social sharing buttons
        binding.btnShareApp.setOnClickListener(v -> {
            ShareUtils.shareApp(this);
        });

        binding.btnShareWhatsapp.setOnClickListener(v -> {
            ShareUtils.shareToWhatsApp(this);
        });

        binding.btnLearnMore.setOnClickListener(v -> {
            showAppFeatures();
        });
    }

    private void showAppFeatures() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.why_veil_chat_title)
                .setMessage(R.string.app_features_message)
                .setPositiveButton(R.string.get_started, (dialog, which) -> {
                    authViewModel.signInAnonymously();
                })
                .setNegativeButton(R.string.share_app, (dialog, which) -> {
                    ShareUtils.shareApp(this);
                })
                .show();
    }
}
