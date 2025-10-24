package com.m3.veilchat.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.m3.veilchat.R;
import com.m3.veilchat.adapters.PersonaAdapter;
import com.m3.veilchat.databinding.DialogCreatePersonaBinding;
import com.m3.veilchat.databinding.FragmentPersonasBinding;
import com.m3.veilchat.models.Persona;
import com.m3.veilchat.models.User;
import com.m3.veilchat.viewmodels.UserViewModel;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PersonasFragment extends Fragment {
    private FragmentPersonasBinding binding;
    private UserViewModel userViewModel;
    private PersonaAdapter personaAdapter;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPersonasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupClickListeners();
    }

    private void setupRecyclerView() {
        personaAdapter = new PersonaAdapter();
        binding.rvPersonas.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPersonas.setAdapter(personaAdapter);

        personaAdapter.setOnPersonaClickListener(new PersonaAdapter.OnPersonaClickListener() {
            @Override
            public void onPersonaClick(Persona persona) {
                showPersonaDetails(persona);
            }

            @Override
            public void onPersonaSwitch(Persona persona) {
                userViewModel.switchPersona(persona.getPersonaId());
                showSwitchSuccess(persona);
            }
        });
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getPersonas() != null) {
                binding.tvNoPersonas.setVisibility(user.getPersonas().isEmpty() ? View.VISIBLE : View.GONE);
                binding.rvPersonas.setVisibility(user.getPersonas().isEmpty() ? View.GONE : View.VISIBLE);
                personaAdapter.submitList(user.getPersonas());
            }
        });

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                binding.tvError.setText(errorMessage);
                binding.tvError.setVisibility(View.VISIBLE);
            } else {
                binding.tvError.setVisibility(View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnAddPersona.setOnClickListener(v -> showCreatePersonaDialog());
    }

    private void showCreatePersonaDialog() {
        DialogCreatePersonaBinding dialogBinding = DialogCreatePersonaBinding.inflate(getLayoutInflater());

        ArrayAdapter<CharSequence> moodAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.moods, android.R.layout.simple_spinner_item);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerMood.setAdapter(moodAdapter);

        ArrayAdapter<CharSequence> interestAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.interests, android.R.layout.simple_spinner_item);
        interestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerInterest.setAdapter(interestAdapter);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.create_persona_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.create, (dialog, which) -> createPersona(dialogBinding))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void createPersona(DialogCreatePersonaBinding dialogBinding) {
        String name = dialogBinding.etPersonaName.getText().toString().trim();
        String displayName = dialogBinding.etDisplayName.getText().toString().trim();
        String mood = dialogBinding.spinnerMood.getSelectedItem().toString();
        String interest = dialogBinding.spinnerInterest.getSelectedItem().toString();

        if (name.isEmpty()) {
            dialogBinding.etPersonaName.setError(getString(R.string.persona_name_required));
            return;
        }

        if (displayName.isEmpty()) {
            displayName = name;
        }

        userViewModel.addPersona(new Persona(name, displayName, mood, interest));
    }

    private void showPersonaDetails(Persona persona) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.persona_details_title)
                .setMessage(getString(R.string.persona_details_message, persona.getName(), persona.getDisplayName(),
                        persona.getMood(), persona.getInterestTag(), dateFormat.format(persona.getCreatedAt())))
                .setPositiveButton(R.string.ok, null)
                .setNeutralButton(R.string.switch_to_this_persona, (dialog, which) -> {
                    userViewModel.switchPersona(persona.getPersonaId());
                    showSwitchSuccess(persona);
                })
                .show();
    }

    private void showSwitchSuccess(Persona persona) {
        binding.tvStatus.setText(getString(R.string.switched_to_persona_status, persona.getDisplayName()));
        binding.tvStatus.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (binding != null) {
                binding.tvStatus.setVisibility(View.GONE);
            }
        }, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
