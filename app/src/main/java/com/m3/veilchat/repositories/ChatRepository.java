package com.m3.veilchat.repositories;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.*;
import com.m3.veilchat.models.ChatRoom;
import com.m3.veilchat.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.m3.veilchat.utils.EncryptionUtils;
import com.m3.veilchat.utils.SecurePrefsManager;
import javax.crypto.SecretKey;
import java.util.*;

public class ChatRepository {
    private static final String TAG = "ChatRepository";
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private SecurePrefsManager securePrefsManager;

    private MutableLiveData<List<ChatRoom>> publicRooms = new MutableLiveData<>();
    private MutableLiveData<List<ChatRoom>> userRooms = new MutableLiveData<>();
    private MutableLiveData<List<Message>> roomMessages = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Cleanup task for expired rooms and messages
    private Timer cleanupTimer;

    public ChatRepository(Context context) {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        securePrefsManager = new SecurePrefsManager(context);
        startCleanupTask();
    }

    private void startCleanupTask() {
        cleanupTimer = new Timer();
        cleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredData();
            }
        }, 0, 60000); // Run every minute
    }

    private void cleanupExpiredData() {
        cleanupExpiredRooms();
        cleanupExpiredMessages();
    }

    private void cleanupExpiredRooms() {
        firestore.collection("chat_rooms")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        ChatRoom room = doc.toObject(ChatRoom.class);
                        if (room != null && room.hasExpired()) {
                            // Deactivate expired room
                            firestore.collection("chat_rooms").document(doc.getId())
                                    .update("isActive", false)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Deactivated expired room: " + doc.getId());
                                    });
                        }
                    }
                });
    }

    private void cleanupExpiredMessages() {
        firestore.collection("messages")
                .whereLessThan("expiresAt", System.currentTimeMillis())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        // Delete expired messages
                        firestore.collection("messages").document(doc.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Deleted expired message: " + doc.getId());
                                });
                    }
                });
    }

    // Enhanced sendMessage with persona support
    public void sendMessage(String roomId, String content, String rules) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        Message message = new Message(roomId, userId, content);
        message.setRules(rules);
        message.setTimestamp(System.currentTimeMillis());

        // Handle different message rules with encryption
        String finalContent = content;

        try {
            if ("cipher".equals(rules)) {
                // For cipher messages, use emoji encryption
                String cipherKey = securePrefsManager.getCipherKey(roomId);
                if (cipherKey == null) {
                    // Generate new cipher key for this room
                    cipherKey = EncryptionUtils.generateCipherKey();
                    securePrefsManager.storeCipherKey(roomId, cipherKey);
                }
                finalContent = EncryptionUtils.encryptWithEmojiCipher(content, cipherKey);
                message.setCipherKey(cipherKey); // Store for demonstration
            } else if ("encrypted".equals(rules)) {
                // For regular encrypted messages
                SecretKey roomKey = securePrefsManager.getRoomKey(roomId);
                if (roomKey == null) {
                    roomKey = EncryptionUtils.generateKey();
                    securePrefsManager.storeRoomKey(roomId, roomKey);
                }
                finalContent = EncryptionUtils.encrypt(content, roomKey);
            }
            // For blink and normal messages, content remains as-is
        } catch (Exception e) {
            Log.e(TAG, "Encryption failed, sending as plain text", e);
            // Continue with original content if encryption fails
        }

        message.setContent(finalContent);

        // Set expiration for blink messages (10 seconds)
        if ("blink".equals(rules)) {
            message.setExpiresAt(System.currentTimeMillis() + 10000);
        }

        // Add sender display name from current persona
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        com.m3.veilchat.models.User user = userDoc.toObject(com.m3.veilchat.models.User.class);
                        if (user != null && user.getCurrentPersonaId() != null) {
                            for (com.m3.veilchat.models.Persona persona : user.getPersonas()) {
                                if (persona.getPersonaId().equals(user.getCurrentPersonaId())) {
                                    message.setSenderDisplayName(persona.getDisplayName());
                                    message.setSenderPersonaId(persona.getPersonaId());
                                    break;
                                }
                            }
                        }
                    }

                    // Send the message
                    firestore.collection("messages")
                            .add(message)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Message sent with ID: " + documentReference.getId());
                                updateRoomActivity(roomId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error sending message", e);
                                errorMessage.setValue("Failed to send message");
                            });
                });
    }

    // Rest of the existing methods remain the same...
    public void loadPublicRooms() {
        firestore.collection("chat_rooms")
                .whereEqualTo("roomType", "public")
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for public rooms", error);
                        errorMessage.setValue("Failed to load rooms");
                        return;
                    }

                    List<ChatRoom> rooms = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatRoom room = doc.toObject(ChatRoom.class);
                            if (room != null && !room.hasExpired()) {
                                room.setRoomId(doc.getId());
                                rooms.add(room);
                            }
                        }
                    }
                    publicRooms.setValue(rooms);
                });
    }

    public void loadUserRooms() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        firestore.collection("chat_rooms")
                .whereArrayContains("participantIds", userId)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for user rooms", error);
                        errorMessage.setValue("Failed to load your rooms");
                        return;
                    }

                    List<ChatRoom> rooms = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatRoom room = doc.toObject(ChatRoom.class);
                            if (room != null && !room.hasExpired()) {
                                room.setRoomId(doc.getId());
                                rooms.add(room);
                            }
                        }
                    }
                    userRooms.setValue(rooms);
                });
    }

    public void createRoom(ChatRoom room) {
        String userId = getCurrentUserId();
        if (userId == null) {
            errorMessage.setValue("You must be logged in to create a room");
            return;
        }

        room.setCreatedBy(userId);
        room.addParticipant(userId);
        room.getAdminIds().add(userId);

        // Set expiration for ephemeral rooms (24 hours)
        if (room.isEphemeral()) {
            room.setExpiresAt(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
        }

        firestore.collection("chat_rooms")
                .add(room)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Room created with ID: " + documentReference.getId());
                    joinRoom(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating room", e);
                    errorMessage.setValue("Failed to create room");
                });
    }

    public void joinRoom(String roomId) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        firestore.collection("chat_rooms").document(roomId)
                .update("participantIds", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User joined room: " + roomId);
                    sendSystemMessage(roomId, "A new user joined the room");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error joining room", e);
                    errorMessage.setValue("Failed to join room");
                });
    }

    public void leaveRoom(String roomId) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        firestore.collection("chat_rooms").document(roomId)
                .update("participantIds", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User left room: " + roomId);
                    sendSystemMessage(roomId, "A user left the room");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error leaving room", e);
                    errorMessage.setValue("Failed to leave room");
                });
    }

    // Enhanced message loading with decryption
    public void loadRoomMessages(String roomId) {
        firestore.collection("messages")
                .whereEqualTo("roomId", roomId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for messages", error);
                        errorMessage.setValue("Failed to load messages");
                        return;
                    }

                    List<Message> messages = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            if (message != null && !message.hasExpired()) {
                                message.setMessageId(doc.getId());

                                // Decrypt messages if needed
                                String content = message.getContent();
                                try {
                                    if ("cipher".equals(message.getRules())) {
                                        String cipherKey = securePrefsManager.getCipherKey(roomId);
                                        if (cipherKey != null) {
                                            content = EncryptionUtils.decryptWithEmojiCipher(content, cipherKey);
                                            message.setContent(content);
                                        }
                                    } else if ("encrypted".equals(message.getRules())) {
                                        SecretKey roomKey = securePrefsManager.getRoomKey(roomId);
                                        if (roomKey != null) {
                                            content = EncryptionUtils.decrypt(content, roomKey);
                                            message.setContent(content);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Decryption failed for message", e);
                                    // Keep encrypted content if decryption fails
                                }

                                messages.add(message);
                            }
                        }
                    }
                    roomMessages.setValue(messages);
                });
    }

    // Create encrypted room
    public void createEncryptedRoom(ChatRoom room, String password) {
        String userId = getCurrentUserId();
        if (userId == null) {
            errorMessage.setValue("You must be logged in to create a room");
            return;
        }

        room.setCreatedBy(userId);
        room.addParticipant(userId);
        room.getAdminIds().add(userId);

        // Set expiration for ephemeral rooms (24 hours)
        if (room.isEphemeral()) {
            room.setExpiresAt(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
        }

        // If password provided, store room access control
        if (password != null && !password.isEmpty()) {
            String passwordHash = EncryptionUtils.hashPassword(password);
            room.setPasswordHash(passwordHash);
        }

        firestore.collection("chat_rooms")
                .add(room)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Encrypted room created with ID: " + documentReference.getId());

                    // Generate and store room encryption key
                    try {
                        SecretKey roomKey = EncryptionUtils.generateKey();
                        securePrefsManager.storeRoomKey(documentReference.getId(), roomKey);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to generate room key", e);
                    }

                    joinRoom(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating encrypted room", e);
                    errorMessage.setValue("Failed to create room");
                });
    }

    // Share cipher key with another user
    public void shareCipherKey(String roomId, String targetUserId, String cipherKey) {
        // In a real implementation, you'd encrypt the cipher key with the target user's public key
        // For now, we'll simulate this by storing it in a secure collection
        Map<String, Object> keyShare = new HashMap<>();
        keyShare.put("roomId", roomId);
        keyShare.put("userId", targetUserId);
        keyShare.put("cipherKey", cipherKey);
        keyShare.put("sharedAt", System.currentTimeMillis());

        firestore.collection("key_shares")
                .add(keyShare)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Cipher key shared successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to share cipher key", e);
                });
    }

    private void sendSystemMessage(String roomId, String content) {
        Message message = new Message(roomId, "system", content);
        message.setMessageType("system");

        firestore.collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "System message sent");
                });
    }

    private void updateRoomActivity(String roomId) {
        firestore.collection("chat_rooms").document(roomId)
                .update("lastActivity", System.currentTimeMillis())
                .addOnFailureListener(e -> Log.e(TAG, "Error updating room activity", e));
    }

    private String getCurrentUserId() {
        return firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
    }

    // LiveData Getters
    public MutableLiveData<List<ChatRoom>> getPublicRooms() { return publicRooms; }
    public MutableLiveData<List<ChatRoom>> getUserRooms() { return userRooms; }
    public MutableLiveData<List<Message>> getRoomMessages() { return roomMessages; }
    public MutableLiveData<String> getErrorMessage() { return errorMessage; }

    public void cleanup() {
        if (cleanupTimer != null) {
            cleanupTimer.cancel();
        }
    }
}