package com.m3.veilchat.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.m3.veilchat.databinding.ActivityChatRoomBinding;
import com.m3.veilchat.fragments.ChatRoomFragment;
import com.m3.veilchat.utils.ScreenshotProtectionUtils;

public class ChatRoomActivity extends AppCompatActivity {
    private ActivityChatRoomBinding binding;
    private String roomId;
    private String roomName;
    private boolean isEphemeralRoom = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get room details
        roomId = getIntent().getStringExtra("roomId");
        roomName = getIntent().getStringExtra("roomName");
        isEphemeralRoom = getIntent().getBooleanExtra("isEphemeral", false);

        // Enable screenshot protection for ephemeral rooms
        if (isEphemeralRoom) {
            ScreenshotProtectionUtils.enableScreenshotProtection(this);
        }

        setupFragment();
        setupToolbar();
    }

    private void setupFragment() {
        ChatRoomFragment fragment = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putString("roomId", roomId);
        args.putString("roomName", roomName);
        args.putBoolean("isEphemeral", isEphemeralRoom);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(binding.fragmentContainer.getId(), fragment)
                .commit();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(roomName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Disable screenshot protection when leaving
        ScreenshotProtectionUtils.disableScreenshotProtection(this);
    }
}