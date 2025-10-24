package com.m3.veilchat.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.m3.veilchat.databinding.DialogLinkEmailBinding;

public class LinkEmailDialog extends DialogFragment {
    private DialogLinkEmailBinding binding;
    private OnEmailLinkedListener listener;

    public interface OnEmailLinkedListener {
        void onEmailLinked(String email, String password);
    }

    public void setOnEmailLinkedListener(OnEmailLinkedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        binding = DialogLinkEmailBinding.inflate(LayoutInflater.from(requireContext()));

        setupClickListeners();

        builder.setView(binding.getRoot())
                .setTitle("Link Email Account")
                .setNegativeButton("Cancel", null);

        return builder.create();
    }

    private void setupClickListeners() {
        binding.btnLinkEmail.setOnClickListener(v -> {
            if (validateInputs()) {
                if (listener != null) {
                    listener.onEmailLinked(
                            binding.etEmail.getText().toString().trim(),
                            binding.etPassword.getText().toString().trim()
                    );
                }
                dismiss();
            }
        });
    }

    private boolean validateInputs() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Invalid email address");
            return false;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
