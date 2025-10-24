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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.m3.veilchat.R;
import com.m3.veilchat.adapters.ChatRoomAdapter;
import com.m3.veilchat.databinding.FragmentChatsBinding;
import com.m3.veilchat.models.ChatRoom;
import com.m3.veilchat.viewmodels.ChatViewModel;

public class ChatsFragment extends Fragment {
    private FragmentChatsBinding binding;
    private ChatViewModel chatViewModel;
    private ChatRoomAdapter chatRoomAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        chatViewModel.loadUserRooms();
    }

    private void setupRecyclerView() {
        chatRoomAdapter = new ChatRoomAdapter();
        binding.rvChatRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvChatRooms.setAdapter(chatRoomAdapter);

        chatRoomAdapter.setOnChatRoomClickListener(chatRoom -> navigateToChatRoom(chatRoom));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ChatRoom room = chatRoomAdapter.getCurrentList().get(position);
                chatViewModel.leaveRoom(room.getRoomId());

                Snackbar.make(binding.getRoot(), "You have left " + room.getName(), Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> chatViewModel.joinRoom(room.getRoomId()))
                        .show();
            }
        }).attachToRecyclerView(binding.rvChatRooms);
    }

    private void setupObservers() {
        chatViewModel.getUserRooms().observe(getViewLifecycleOwner(), chatRooms -> {
            if (chatRooms != null && !chatRooms.isEmpty()) {
                binding.tvNoChats.setVisibility(View.GONE);
                binding.rvChatRooms.setVisibility(View.VISIBLE);
                chatRoomAdapter.submitList(chatRooms);
            } else {
                binding.tvNoChats.setVisibility(View.VISIBLE);
                binding.rvChatRooms.setVisibility(View.GONE);
            }
        });

        chatViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                binding.tvError.setText(errorMessage);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupClickListeners() {
        binding.fabCreateRoom.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_chats_to_createRoom));
    }

    private void navigateToChatRoom(ChatRoom chatRoom) {
        Bundle bundle = new Bundle();
        bundle.putString("roomId", chatRoom.getRoomId());
        bundle.putString("roomName", chatRoom.getName());
        Navigation.findNavController(requireView()).navigate(R.id.action_chats_to_chatRoom, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
