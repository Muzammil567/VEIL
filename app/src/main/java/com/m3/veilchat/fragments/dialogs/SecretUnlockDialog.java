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

public class SecretUnlockDialog extends DialogFragment {
    private OnUnlockListener unlockListener;

    public interface OnUnlockListener {
        void onUnlock(String password);
    }

    public void setOnUnlockListener(OnUnlockListener listener) {
        this.unlockListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_secret_unlock, null);

        TextInputEditText etPassword = view.findViewById(R.id.etSecretPassword);

        builder.setView(view)
                .setTitle("Unlock Secret Inbox")
                .setMessage("Enter your secret inbox password")
                .setPositiveButton("Unlock", (dialog, id) -> {
                    String password = etPassword.getText().toString().trim();
                    if (unlockListener != null) {
                        unlockListener.onUnlock(password);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });

        return builder.create();
    }
}