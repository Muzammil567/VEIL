package com.m3.veilchat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.m3.veilchat.adapters.ConfessionAdapter;
import com.m3.veilchat.databinding.FragmentConfessionBoardBinding;
import com.m3.veilchat.models.Confession;
import com.m3.veilchat.viewmodels.ConfessionViewModel;

public class ConfessionBoardFragment extends Fragment {
    private FragmentConfessionBoardBinding binding;
    private ConfessionViewModel confessionViewModel;
    private ConfessionAdapter confessionAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConfessionBoardBinding.inflate(inflater, container, false);
        confessionViewModel = new ViewModelProvider(this).get(ConfessionViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        // Load confessions
        confessionViewModel.loadConfessions();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        confessionAdapter = new ConfessionAdapter();
        binding.rvConfessions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvConfessions.setAdapter(confessionAdapter);

        confessionAdapter.setOnConfessionClickListener(new ConfessionAdapter.OnConfessionClickListener() {
            @Override
            public void onConfessionClick(Confession confession) {
                showConfessionOptions(confession);
            }

            @Override
            public void onReplyClick(Confession confession) {
                showReplyDialog(confession);
            }

            @Override
            public void onLikeClick(Confession confession) {
                confessionViewModel.likeConfession(confession.getConfessionId());
            }
        });
    }

    private void setupObservers() {
        confessionViewModel.getConfessions().observe(getViewLifecycleOwner(), confessions -> {
            if (confessions != null && !confessions.isEmpty()) {
                binding.tvNoConfessions.setVisibility(View.GONE);
                binding.rvConfessions.setVisibility(View.VISIBLE);
                confessionAdapter.setConfessions(confessions);
            } else {
                binding.tvNoConfessions.setVisibility(View.VISIBLE);
                binding.rvConfessions.setVisibility(View.GONE);
            }
        });

        confessionViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                binding.tvError.setText(errorMessage);
                binding.tvError.setVisibility(View.VISIBLE);
            } else {
                binding.tvError.setVisibility(View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        binding.fabAddConfession.setOnClickListener(v -> {
            showAddConfessionDialog();
        });
    }

    private void showAddConfessionDialog() {
        AddConfessionDialog dialog = new AddConfessionDialog();
        dialog.setOnConfessionPostedListener((content, mood) -> {
            confessionViewModel.postConfession(content, mood);
        });
        dialog.show(getParentFragmentManager(), "AddConfessionDialog");
    }

    private void showConfessionOptions(Confession confession) {
        // Show options menu for confession
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confession Options")
                .setItems(new String[]{"Reply Privately", "Share", "Report"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Reply
                            showReplyDialog(confession);
                            break;
                        case 1: // Share
                            shareConfession(confession);
                            break;
                        case 2: // Report
                            reportConfession(confession);
                            break;
                    }
                })
                .show();
    }

    private void showReplyDialog(Confession confession) {
        ReplyToConfessionDialog dialog = new ReplyToConfessionDialog();
        dialog.setOnReplySentListener(message -> {
            confessionViewModel.replyToConfession(confession.getConfessionId(), message);
        });
        dialog.show(getParentFragmentManager(), "ReplyToConfessionDialog");
    }

    private void shareConfession(Confession confession) {
        // Implement sharing functionality
    }

    private void reportConfession(Confession confession) {
        // Implement reporting functionality
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}