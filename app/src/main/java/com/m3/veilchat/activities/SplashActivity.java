package com.m3.veilchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.m3.veilchat.R;
import com.m3.veilchat.viewmodels.AuthViewModel;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAuthStatus(authViewModel);
        }, SPLASH_DELAY);
    }

    private void checkAuthStatus(AuthViewModel authViewModel) {
        if (authViewModel.isUserLoggedIn()) {
            // User is already logged in, go to main activity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // User needs to sign in, go to welcome activity
            startActivity(new Intent(this, WelcomeActivity.class));
        }
        finish();
    }
}
