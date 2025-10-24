package com.m3.veilchat.managers;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WhisperLinkManager {
    private static final String TAG = "WhisperLinkManager";
    private FirebaseFirestore firestore;

    public WhisperLinkManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    // Generate single-use chat link
    public void createWhisperLink(WhisperLinkCallback callback) {
        String linkCode = generateLinkCode();
        String linkId = "whisper_" + System.currentTimeMillis();

        Map<String, Object> linkData = new HashMap<>();
        linkData.put("linkId", linkId);
        linkData.put("linkCode", linkCode);
        linkData.put("createdAt", System.currentTimeMillis());
        linkData.put("isUsed", false);
        linkData.put("creatorId", getCurrentUserId());
        linkData.put("participantCount", 0);
        linkData.put("maxParticipants", 2); // 1-on-1 chats

        firestore.collection("whisper_links").document(linkId)
                .set(linkData)
                .addOnSuccessListener(aVoid -> {
                    String shareableLink = "veil.chat/whisper/" + linkCode;
                    callback.onLinkCreated(shareableLink, linkId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create whisper link", e);
                    callback.onLinkCreationFailed("Failed to create link");
                });
    }

    // Join via whisper link
    public void joinWhisperLink(String linkCode, WhisperJoinCallback callback) {
        firestore.collection("whisper_links")
                .whereEqualTo("linkCode", linkCode)
                .whereEqualTo("isUsed", false)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String linkId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        Map<String, Object> linkData = queryDocumentSnapshots.getDocuments().get(0).getData();

                        // Check if already has participants
                        Long participantCount = (Long) linkData.get("participantCount");
                        Long maxParticipants = (Long) linkData.get("maxParticipants");

                        if (participantCount >= maxParticipants) {
                            callback.onJoinFailed("Link already in use");
                            return;
                        }

                        // Increment participant count
                        firestore.collection("whisper_links").document(linkId)
                                .update("participantCount", participantCount + 1)
                                .addOnSuccessListener(aVoid -> {
                                    // Mark as used when both participants join
                                    if (participantCount + 1 >= maxParticipants) {
                                        firestore.collection("whisper_links").document(linkId)
                                                .update("isUsed", true);
                                    }

                                    // Create or get chat room for this whisper link
                                    createWhisperRoom(linkId, linkCode, callback);
                                });
                    } else {
                        callback.onJoinFailed("Invalid or expired link");
                    }
                });
    }

    private void createWhisperRoom(String linkId, String linkCode, WhisperJoinCallback callback) {
        String roomId = "whisper_room_" + linkId;
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("roomId", roomId);
        roomData.put("name", "Whisper Chat");
        roomData.put("description", "Temporary whisper conversation");
        roomData.put("roomType", "whisper");
        roomData.put("whisperLinkId", linkId);
        roomData.put("createdAt", System.currentTimeMillis());
        roomData.put("expiresAt", System.currentTimeMillis() + (2 * 60 * 60 * 1000)); // 2 hours
        roomData.put("selfDestructAfterLeave", true);

        firestore.collection("chat_rooms").document(roomId)
                .set(roomData)
                .addOnSuccessListener(aVoid -> {
                    callback.onJoinSuccess(roomId, "Whisper Chat");
                })
                .addOnFailureListener(e -> {
                    callback.onJoinFailed("Failed to create chat");
                });
    }

    // Self-destruct when both users leave
    public void leaveWhisperRoom(String roomId, String linkId) {
        // Decrement participant count
        firestore.collection("whisper_links").document(linkId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long participantCount = documentSnapshot.getLong("participantCount");
                        if (participantCount != null && participantCount > 0) {
                            firestore.collection("whisper_links").document(linkId)
                                    .update("participantCount", participantCount - 1);

                            // If no participants left, delete the room
                            if (participantCount - 1 <= 0) {
                                firestore.collection("chat_rooms").document(roomId).delete();
                                firestore.collection("whisper_links").document(linkId).delete();
                            }
                        }
                    }
                });
    }

    private String generateLinkCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private String getCurrentUserId() {
        // Implement based on your auth system
        return null;
    }

    public interface WhisperLinkCallback {
        void onLinkCreated(String shareableLink, String linkId);
        void onLinkCreationFailed(String error);
    }

    public interface WhisperJoinCallback {
        void onJoinSuccess(String roomId, String roomName);
        void onJoinFailed(String error);
    }
}