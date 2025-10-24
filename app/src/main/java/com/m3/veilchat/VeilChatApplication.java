package com.m3.veilchat;

import android.app.Application;
import com.m3.veilchat.repositories.ChatRepository;

public class VeilChatApplication extends Application {
    private ChatRepository chatRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize global components
        chatRepository = new ChatRepository();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Cleanup resources
        if (chatRepository != null) {
            chatRepository.cleanup();
        }
    }
}