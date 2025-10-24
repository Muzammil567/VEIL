package com.m3.veilchat.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.m3.veilchat.R;
import com.m3.veilchat.managers.WhisperLinkManager;
import com.m3.veilchat.utils.ShareUtils;

public class CreateWhisperDialog extends DialogFragment {
    private WhisperLinkManager whisperLinkManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_create_whisper, null);

        whisperLinkManager = new WhisperLinkManager(requireContext());

        Spinner spinnerUses = view.findViewById(R.id.spinnerUses);
        Spinner spinnerExpiry = view.findViewById(R.id.spinnerExpiry);

        // Setup uses spinner
        String[] uses = {"1 use", "3 uses", "5 uses", "10 uses", "Unlimited"};
        ArrayAdapter<String> usesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, uses);
        usesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUses.setAdapter(usesAdapter);

        // Setup expiry spinner
        String[] expiry = {"1 hour", "6 hours", "12 hours", "1 day", "3 days", "1 week"};
        ArrayAdapter<String> expiryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, expiry);
        expiryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpiry.setAdapter(expiryAdapter);

        builder.setView(view)
                .setTitle("Create Whisper Link")
                .setPositiveButton("Create Link", (dialog, id) -> {
                    int maxUses = parseUses(spinnerUses.getSelectedItem().toString());
                    long expiryHours = parseExpiry(spinnerExpiry.getSelectedItem().toString());

                    whisperLinkManager.createWhisperLink(maxUses, expiryHours, new WhisperLinkManager.OnLinkCreatedListener() {
                        @Override
                        public void onLinkCreated(String linkId, String shareableUrl) {
                            // Share the link
                            ShareUtils.shareWhisperLink(requireContext(), shareableUrl);
                        }

                        @Override
                        public void onError(String error) {
                            // Show error toast
                            android.widget.Toast.makeText(requireContext(), "Failed to create link: " + error, android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });

        return builder.create();
    }

    private int parseUses(String uses) {
        switch (uses) {
            case "1 use": return 1;
            case "3 uses": return 3;
            case "5 uses": return 5;
            case "10 uses": return 10;
            case "Unlimited": return 999;
            default: return 1;
        }
    }

    private long parseExpiry(String expiry) {
        switch (expiry) {
            case "1 hour": return 1;
            case "6 hours": return 6;
            case "12 hours": return 12;
            case "1 day": return 24;
            case "3 days": return 72;
            case "1 week": return 168;
            default: return 24;
        }
    }
}