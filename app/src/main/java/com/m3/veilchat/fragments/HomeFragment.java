package com.m3.veilchat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.m3.veilchat.R;
import com.m3.veilchat.adapters.RecentActivityAdapter;
import com.m3.veilchat.databinding.FragmentHomeBinding;
import com.m3.veilchat.models.ChatRoom;
import com.m3.veilchat.models.RecentActivity;
import com.m3.veilchat.models.User;
import com.m3.veilchat.viewmodels.ChatViewModel;
import com.m3.veilchat.viewmodels.UserViewModel;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private UserViewModel userViewModel;
    private ChatViewModel chatViewModel;
    private RecentActivityAdapter recentActivityAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        chatViewModel.loadUserRooms();
    }

    private void setupRecyclerView() {
        recentActivityAdapter = new RecentActivityAdapter();
        binding.rvRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRecentActivity.setAdapter(recentActivityAdapter);

        recentActivityAdapter.setOnActivityClickListener(activity -> {
            if (activity.getRoomId() != null) {
                navigateToChatRoom(activity.getRoomId(), activity.getRoomName());
            }
        });
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::updateUI);

        chatViewModel.getUserRooms().observe(getViewLifecycleOwner(), chatRooms -> {
            if (chatRooms != null && !chatRooms.isEmpty()) {
                updateRecentActivity(chatRooms);
            } else {
                binding.tvNoActivity.setVisibility(View.VISIBLE);
                binding.rvRecentActivity.setVisibility(View.GONE);
            }
        });

        chatViewModel.getRoomCreationStatus().observe(getViewLifecycleOwner(), room -> {
            if (room != null) {
                navigateToChatRoom(room.getRoomId(), room.getName());
            }
        });
    }

    private void updateUI(User user) {
        if (user != null) {
            if (user.getCurrentPersonaId() != null && user.getPersonas() != null && !user.getPersonas().isEmpty()) {
                for (com.m3.veilchat.models.Persona persona : user.getPersonas()) {
                    if (persona.getPersonaId().equals(user.getCurrentPersonaId())) {
                        binding.tvCurrentPersona.setText(getString(R.string.current_persona, persona.getDisplayName()));
                        break;
                    }
                }
            } else {
                binding.tvCurrentPersona.setText(R.string.no_persona);
            }

            if (user.getPersonas() != null) {
                int personaCount = user.getPersonas().size();
                binding.tvPersonaCount.setText(getResources().getQuantityString(R.plurals.persona_count, personaCount, personaCount));
            }
        }
    }

    private void updateRecentActivity(List<ChatRoom> chatRooms) {
        List<RecentActivity> activities = new ArrayList<>();
        for (ChatRoom room : chatRooms) {
            activities.add(new RecentActivity(room.getRoomId(), room.getName(), "Joined room", room.getCreatedAt(), RecentActivity.TYPE_ROOM_JOIN));
        }

        if (activities.isEmpty()) {
            binding.tvNoActivity.setVisibility(View.VISIBLE);
            binding.rvRecentActivity.setVisibility(View.GONE);
        } else {
            binding.tvNoActivity.setVisibility(View.GONE);
            binding.rvRecentActivity.setVisibility(View.VISIBLE);
            recentActivityAdapter.submitList(activities);
        }
    }

    private void setupClickListeners() {
        binding.btnCreatePersona.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.navigation_personas));

        binding.btnJoinRoom.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.navigation_discover));

        binding.btnQuickCreateRoom.setOnClickListener(v -> showCreateRoomDialog());
    }

    private void showCreateRoomDialog() {
        CreateRoomDialog dialog = new CreateRoomDialog();
        dialog.setOnRoomCreatedListener(chatViewModel::createRoom);
        dialog.show(getParentFragmentManager(), "CreateRoomDialog");
    }

    private void navigateToChatRoom(String roomId, String roomName) {
        Bundle bundle = new Bundle();
        bundle.putString("roomId", roomId);
        bundle.putString("roomName", roomName);
        Navigation.findNavController(requireView()).navigate(R.id.action_global_chatRoom, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
