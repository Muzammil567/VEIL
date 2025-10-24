package com.m3.veilchat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.m3.veilchat.R;
import com.m3.veilchat.adapters.ChatRoomAdapter;
import com.m3.veilchat.databinding.FragmentDiscoverBinding;
import com.m3.veilchat.models.ChatRoom;
import com.m3.veilchat.viewmodels.ChatViewModel;
import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {
    private FragmentDiscoverBinding binding;
    private ChatViewModel chatViewModel;
    private ChatRoomAdapter chatRoomAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        setupFilterSpinners();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        chatViewModel.loadPublicRooms();
    }

    private void setupFilterSpinners() {
        ArrayAdapter<CharSequence> moodAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.mood_filters, android.R.layout.simple_spinner_item);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMood.setAdapter(moodAdapter);

        ArrayAdapter<CharSequence> interestAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.interest_filters, android.R.layout.simple_spinner_item);
        interestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerInterest.setAdapter(interestAdapter);
    }

    private void setupRecyclerView() {
        chatRoomAdapter = new ChatRoomAdapter();
        binding.rvRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRooms.setAdapter(chatRoomAdapter);

        chatRoomAdapter.setOnChatRoomClickListener(chatRoom -> {
            chatViewModel.joinRoom(chatRoom.getRoomId());
            navigateToChatRoom(chatRoom);
        });
    }

    private void setupObservers() {
        chatViewModel.getPublicRooms().observe(getViewLifecycleOwner(), chatRooms -> {
            if (chatRooms != null) {
                applyFilters(chatRooms);
            }
        });

        chatViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                binding.tvError.setText(errorMessage);
                binding.tvError.setVisibility(View.VISIBLE);
            } else {
                binding.tvError.setVisibility(View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnApplyFilters.setOnClickListener(v -> {
            List<ChatRoom> currentRooms = chatViewModel.getPublicRooms().getValue();
            if (currentRooms != null) {
                applyFilters(currentRooms);
            }
        });

        binding.fabCreateRoom.setOnClickListener(v -> showCreateRoomDialog());

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            List<ChatRoom> currentRooms = chatViewModel.getPublicRooms().getValue();
            if (currentRooms != null) {
                applyFilters(currentRooms);
            }
            return true;
        });
    }

    private void applyFilters(List<ChatRoom> allRooms) {
        String searchQuery = binding.etSearch.getText().toString().trim();
        String selectedMood = binding.spinnerMood.getSelectedItem().toString();
        String selectedInterest = binding.spinnerInterest.getSelectedItem().toString();

        List<ChatRoom> filteredRooms = new ArrayList<>();
        for (ChatRoom room : allRooms) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    room.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    room.getDescription().toLowerCase().contains(searchQuery.toLowerCase());

            boolean matchesMood = selectedMood.equals("All Moods") ||
                    (room.getMoodTag() != null && room.getMoodTag().equals(selectedMood));

            boolean matchesInterest = selectedInterest.equals("All Interests") ||
                    (room.getInterestTag() != null && room.getInterestTag().equals(selectedInterest));

            if (matchesSearch && matchesMood && matchesInterest) {
                filteredRooms.add(room);
            }
        }

        chatRoomAdapter.submitList(filteredRooms);

        binding.tvNoRooms.setVisibility(filteredRooms.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvRooms.setVisibility(filteredRooms.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showCreateRoomDialog() {
        CreateRoomDialog dialog = new CreateRoomDialog();
        dialog.setOnRoomCreatedListener(room -> {
            chatViewModel.createRoom(room);
            Navigation.findNavController(requireView()).navigateUp();
        });
        dialog.show(getParentFragmentManager(), "CreateRoomDialog");
    }

    private void navigateToChatRoom(ChatRoom chatRoom) {
        Bundle bundle = new Bundle();
        bundle.putString("roomId", chatRoom.getRoomId());
        bundle.putString("roomName", chatRoom.getName());
        Navigation.findNavController(requireView()).navigate(R.id.action_discover_to_chatRoom, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
