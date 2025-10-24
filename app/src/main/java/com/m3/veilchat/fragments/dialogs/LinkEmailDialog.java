package com.m3.veilchat.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.m3.veilchat.R;

public class LinkEmailDialog extends DialogFragment {
    private OnEmailLinkedListener emailLinkedListener;

    public interface OnEmailLinkedListener {
        void onEmailLinked(String email, String password);
    }

    public void setOnEmailLinkedListener(OnEmailLinkedListener listener) {
        this.emailLinkedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_link_email, null);

        TextInputEditText etEmail = view.findViewById(R.id.etEmail);
        TextInputEditText etPassword = view.findViewById(R.id.etPassword);
        TextInputEditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        builder.setView(view)
                .setTitle("Link Email Account")
                .setMessage("Link an email to your account for backup and recovery")
                .setPositiveButton("Link Account", (dialog, id) -> {
                    String email = etEmail.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText().toString().trim();

                    if (email.isEmpty()) {
                        etEmail.setError("Email is required");
                        return;
                    }

                    if (password.isEmpty()) {
                        etPassword.setError("Password is required");
                        return;
                    }

                    if (!password.equals(confirmPassword)) {
                        etConfirmPassword.setError("Passwords do not match");
                        return;
                    }

                    if (password.length() < 6) {
                        etPassword.setError("Password must be at least 6 characters");
                        return;
                    }

                    if (emailLinkedListener != null) {
                        emailLinkedListener.onEmailLinked(email, password);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });

        return builder.create();
    }
}