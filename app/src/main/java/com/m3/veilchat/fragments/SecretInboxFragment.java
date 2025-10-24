package com.m3.veilchat.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.m3.veilchat.R;
import com.m3.veilchat.adapters.SecretMessageAdapter;
import com.m3.veilchat.databinding.FragmentSecretInboxBinding;
import com.m3.veilchat.fragments.dialogs.SecretUnlockDialog;
import com.m3.veilchat.models.SecretMessage;
import com.m3.veilchat.utils.EncryptionUtils;
import com.m3.veilchat.utils.SecurePrefsManager;
import com.m3.veilchat.viewmodels.SecretInboxViewModel;
import com.m3.veilchat.viewmodels.UserViewModel;

public class SecretInboxFragment extends Fragment {
    private FragmentSecretInboxBinding binding;
    private SecretInboxViewModel secretInboxViewModel;
    private UserViewModel userViewModel;
    private SecretMessageAdapter secretMessageAdapter;
    private SecurePrefsManager securePrefsManager;
    private GestureDetector gestureDetector;
    private boolean isUnlocked = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecretInboxBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        secretInboxViewModel = new ViewModelProvider(this).get(SecretInboxViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        securePrefsManager = new SecurePrefsManager(requireContext());

        setupGestureDetector();
        checkLockStatus();
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showUnlockDialog();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                showUnlockDialog();
            }
        });

        binding.getRoot().setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void checkLockStatus() {
        if (securePrefsManager.isSecretInboxEnabled()) {
            if (isUnlocked) {
                showSecretContent();
            } else {
                showLockScreen();
            }
        } else {
            showSetupScreen();
        }
    }

    private void showLockScreen() {
        binding.lockScreen.setVisibility(View.VISIBLE);
        binding.secretContent.setVisibility(View.GONE);
        binding.setupScreen.setVisibility(View.GONE);

        binding.tvLockMessage.setText(R.string.unlock_secret_inbox_message);
        binding.ivLockIcon.setImageResource(R.drawable.ic_lock);
    }

    private void showSecretContent() {
        binding.lockScreen.setVisibility(View.GONE);
        binding.secretContent.setVisibility(View.VISIBLE);
        binding.setupScreen.setVisibility(View.GONE);

        setupRecyclerView();
        setupObservers();
        loadSecretMessages();
    }

    private void showSetupScreen() {
        binding.lockScreen.setVisibility(View.GONE);
        binding.secretContent.setVisibility(View.GONE);
        binding.setupScreen.setVisibility(View.VISIBLE);

        binding.btnSetupSecretInbox.setOnClickListener(v -> setupSecretInbox());
    }

    private void setupSecretInbox() {
        String password = binding.etSecretPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmSecretPassword.getText().toString().trim();

        if (password.isEmpty()) {
            binding.etSecretPassword.setError(getString(R.string.password_required));
            return;
        }

        if (password.length() < 4) {
            binding.etSecretPassword.setError(getString(R.string.password_too_short));
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmSecretPassword.setError(getString(R.string.passwords_do_not_match));
            return;
        }

        securePrefsManager.setSecretInboxPassword(password);
        securePrefsManager.setSecretInboxEnabled(true);
        isUnlocked = true;

        showSecretContent();
    }

    private void showUnlockDialog() {
        SecretUnlockDialog dialog = new SecretUnlockDialog();
        dialog.setOnUnlockListener(password -> {
            if (securePrefsManager.validateSecretInboxPassword(password)) {
                isUnlocked = true;
                showSecretContent();
            } else {
                binding.tvLockMessage.setText(R.string.wrong_password_unlock_message);
                binding.tvLockMessage.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            }
        });
        dialog.show(getParentFragmentManager(), "SecretUnlockDialog");
    }

    private void setupRecyclerView() {
        String currentUserId = userViewModel.getCurrentUser().getValue() != null ? userViewModel.getCurrentUser().getValue().getUserId() : "";
        secretMessageAdapter = new SecretMessageAdapter(currentUserId);
        binding.rvSecretMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSecretMessages.setAdapter(secretMessageAdapter);

        secretMessageAdapter.setOnMessageClickListener(this::showSecretMessageDetails);
        secretMessageAdapter.setOnDeleteClickListener(this::deleteSecretMessage);
    }

    private void setupObservers() {
        secretInboxViewModel.getSecretMessages().observe(getViewLifecycleOwner(), messages -> {
            binding.tvNoSecretMessages.setVisibility(messages == null || messages.isEmpty() ? View.VISIBLE : View.GONE);
            binding.rvSecretMessages.setVisibility(messages == null || messages.isEmpty() ? View.GONE : View.VISIBLE);
            secretMessageAdapter.submitList(messages);
        });

        secretInboxViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> 
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        secretInboxViewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showError);
    }

    private void loadSecretMessages() {
        secretInboxViewModel.loadSecretMessages();
    }

    private void showSecretMessageDetails(SecretMessage message) {
        String content = message.getContent();
        if (message.isEncrypted() && message.getEncryptionKey() != null) {
            try {
                content = EncryptionUtils.decryptWithEmojiCipher(content, message.getEncryptionKey());
            } catch (Exception e) {
                content = getString(R.string.unable_to_decrypt_message);
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.secret_message_title)
                .setMessage(content)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.delete, (dialog, which) -> deleteSecretMessage(message))
                .show();
    }

    private void deleteSecretMessage(SecretMessage message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_secret_message_title)
                .setMessage(R.string.delete_secret_message_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> secretInboxViewModel.deleteSecretMessage(message.getMessageId()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showError(String error) {
        if (error == null) return;
        binding.tvError.setText(error);
        binding.tvError.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (binding != null) {
                binding.tvError.setVisibility(View.GONE);
            }
        }, 3000);
    }

    @Override
    public void onPause() {
        super.onPause();
        isUnlocked = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
