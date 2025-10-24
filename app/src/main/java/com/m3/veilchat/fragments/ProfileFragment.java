package com.m3.veilchat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.m3.veilchat.R;
import com.m3.veilchat.activities.WelcomeActivity;
import com.m3.veilchat.databinding.FragmentProfileBinding;
import com.m3.veilchat.models.User;
import com.m3.veilchat.utils.ShareUtils;
import com.m3.veilchat.viewmodels.AuthViewModel;
import com.m3.veilchat.viewmodels.UserViewModel;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(User user) {
        if (user != null) {
            binding.tvUsername.setText(getString(R.string.username_format, user.getUsername()));
            binding.tvAnonymousId.setText(getString(R.string.anonymous_id_format, user.getAnonymousId()));

            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                binding.tvEmail.setText(user.getEmail());
                binding.tvEmailStatus.setText(getString(R.string.email_verified_format, String.valueOf(user.isEmailVerified())));
                binding.btnLinkEmail.setVisibility(View.GONE);
                binding.emailSection.setVisibility(View.VISIBLE);
            } else {
                binding.emailSection.setVisibility(View.GONE);
                binding.btnLinkEmail.setVisibility(View.VISIBLE);
            }

            if (user.getPersonas() != null) {
                int personaCount = user.getPersonas().size();
                binding.tvPersonaCount.setText(getResources().getQuantityString(R.plurals.persona_count, personaCount, personaCount));
            }
        }
    }

    private void setupClickListeners() {
        binding.btnShareApp.setOnClickListener(v -> ShareUtils.shareApp(requireContext()));
        binding.btnShareWhatsapp.setOnClickListener(v -> ShareUtils.shareToWhatsApp(requireContext()));
        binding.btnShareUsername.setOnClickListener(v -> {
            User currentUser = userViewModel.getCurrentUser().getValue();
            if (currentUser != null) {
                ShareUtils.shareUsername(requireContext(), currentUser.getUsername());
            }
        });
        binding.btnInviteFriends.setOnClickListener(v -> showInviteOptions());
        binding.btnLinkEmail.setOnClickListener(v -> showLinkEmailDialog());
        binding.btnLogout.setOnClickListener(v -> {
            authViewModel.signOut();
            startActivity(new Intent(requireContext(), WelcomeActivity.class));
            requireActivity().finish();
        });
    }

    private void showInviteOptions() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.invite_friends_title)
                .setItems(R.array.invite_options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            ShareUtils.shareApp(requireContext());
                            break;
                        case 1:
                            ShareUtils.shareToWhatsApp(requireContext());
                            break;
                        case 2:
                            User currentUser = userViewModel.getCurrentUser().getValue();
                            if (currentUser != null) {
                                ShareUtils.shareUsername(requireContext(), currentUser.getUsername());
                            }
                            break;
                        case 3:
                            // createWhisperLink();
                            break;
                        case 4:
                            ShareUtils.shareToTelegram(requireContext());
                            break;
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showLinkEmailDialog() {
        LinkEmailDialog dialog = new LinkEmailDialog();
        dialog.setOnEmailLinkedListener((email, password) -> authViewModel.linkEmail(email, password));
        dialog.show(getParentFragmentManager(), "LinkEmailDialog");
    }

    // Add to ProfileFragment
    private void setupTrustBadges() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // Display trust badges
                int trustScore = user.getTrustScore() != null ? user.getTrustScore() : 0;
                binding.tvTrustScore.setText("Trust Score: " + trustScore);

                // Show badges based on trust level
                if (trustScore >= 10) {
                    binding.ivTrustBadge1.setVisibility(View.VISIBLE);
                }
                if (trustScore >= 25) {
                    binding.ivTrustBadge2.setVisibility(View.VISIBLE);
                }
                if (trustScore >= 50) {
                    binding.ivTrustBadge3.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
