package com.m3.veilchat.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.m3.veilchat.R;
import com.m3.veilchat.databinding.DialogCreateRoomBinding;
import com.m3.veilchat.models.ChatRoom;

public class CreateRoomDialog extends DialogFragment {
    private DialogCreateRoomBinding binding;
    private OnRoomCreatedListener listener;

    public interface OnRoomCreatedListener {
        void onRoomCreated(ChatRoom room);
    }

    public void setOnRoomCreatedListener(OnRoomCreatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        binding = DialogCreateRoomBinding.inflate(LayoutInflater.from(requireContext()));

        setupSpinners();
        setupClickListeners();

        builder.setView(binding.getRoot())
                .setTitle("Create New Room")
                .setNegativeButton("Cancel", null);

        return builder.create();
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.room_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRoomType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> moodAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.moods, android.R.layout.simple_spinner_item);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMood.setAdapter(moodAdapter);

        ArrayAdapter<CharSequence> interestAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.interests, android.R.layout.simple_spinner_item);
        interestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerInterest.setAdapter(interestAdapter);
    }

    private void setupClickListeners() {
        binding.btnCreateRoom.setOnClickListener(v -> {
            if (validateInputs()) {
                createRoom();
            }
        });
    }

    private boolean validateInputs() {
        if (binding.etRoomName.getText().toString().trim().isEmpty()) {
            binding.etRoomName.setError("Room name is required");
            return false;
        }

        if (binding.etRoomDescription.getText().toString().trim().isEmpty()) {
            binding.etRoomDescription.setError("Room description is required");
            return false;
        }

        return true;
    }

    private void createRoom() {
        String roomName = binding.etRoomName.getText().toString().trim();
        String roomDescription = binding.etRoomDescription.getText().toString().trim();
        String roomType = binding.spinnerRoomType.getSelectedItem().toString().toLowerCase();
        String mood = binding.spinnerMood.getSelectedItem().toString();
        String interest = binding.spinnerInterest.getSelectedItem().toString();
        int maxParticipants = Integer.parseInt(binding.etMaxParticipants.getText().toString());

        ChatRoom room = new ChatRoom(roomName, roomDescription, "user", roomType);
        room.setMoodTag(mood);
        room.setInterestTag(interest);
        room.setMaxParticipants(maxParticipants);

        if ("ephemeral".equals(roomType)) {
            room.setExpiresAt(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
        }

        if (listener != null) {
            listener.onRoomCreated(room);
        }

        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
