package com.m3.veilchat.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.m3.veilchat.models.ChatRoom;
import com.m3.veilchat.models.Message;
import com.m3.veilchat.repositories.ChatRepository;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private final ChatRepository chatRepository;

    public ChatViewModel() {
        chatRepository = new ChatRepository();
    }

    public void loadPublicRooms() {
        chatRepository.loadPublicRooms();
    }

    public void loadUserRooms() {
        chatRepository.loadUserRooms();
    }

    public void createRoom(ChatRoom room) {
        chatRepository.createRoom(room);
    }

    public void joinRoom(String roomId) {
        chatRepository.joinRoom(roomId);
    }

    public void leaveRoom(String roomId) {
        chatRepository.leaveRoom(roomId);
    }

    public void loadRoomMessages(String roomId) {
        chatRepository.loadRoomMessages(roomId);
    }

    public void sendMessage(String roomId, String content, String rule) {
        chatRepository.sendMessage(roomId, content, rule);
    }

    public LiveData<List<ChatRoom>> getPublicRooms() {
        return chatRepository.getPublicRooms();
    }

    public LiveData<List<ChatRoom>> getUserRooms() {
        return chatRepository.getUserRooms();
    }

    public LiveData<List<Message>> getRoomMessages() {
        return chatRepository.getRoomMessages();
    }

    public LiveData<ChatRoom> getRoomCreationStatus() {
        return chatRepository.getRoomCreationStatus();
    }

    public LiveData<String> getErrorMessage() {
        return chatRepository.getErrorMessage();
    }
}
