package com.m3.veilchat.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.m3.veilchat.R;
import com.m3.veilchat.models.Confession;

public class ConfessionDetailDialog extends DialogFragment {
    private static final String ARG_CONFESSION = "confession";

    private Confession confession;
    private OnReplyListener replyListener;

    public interface OnReplyListener {
        void onReply(Confession confession);
    }

    public static ConfessionDetailDialog newInstance(Confession confession) {
        ConfessionDetailDialog fragment = new ConfessionDetailDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CONFESSION, confession);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnReplyListener(OnReplyListener listener) {
        this.replyListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            confession = getArguments().getParcelable(ARG_CONFESSION);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String message = "Mood: " + (confession != null ? confession.getMood() : "") + "\n\n" +
                "Category: " + (confession != null ? confession.getCategory() : "") + "\n\n" +
                "Confession:\n" + (confession != null ? confession.getContent() : "");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confession Details")
                .setMessage(message)
                .setPositiveButton("Reply Privately", (dialog, id) -> {
                    if (replyListener != null && confession != null) {
                        replyListener.onReply(confession);
                    }
                })
                .setNegativeButton("Close", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }
}