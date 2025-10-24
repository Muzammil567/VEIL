package com.m3.veilchat.repositories;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.*;
import com.m3.veilchat.models.Confession;
import com.google.firebase.auth.FirebaseAuth;
import java.util.*;

public class ConfessionRepository {
    private static final String TAG = "ConfessionRepository";
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    private MutableLiveData<List<Confession>> confessions = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ConfessionRepository() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    // Post a new confession
    public void postConfession(String content, String moodTag) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        // Get current persona for display name
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        com.m3.veilchat.models.User user = userDoc.toObject(com.m3.veilchat.models.User.class);
                        if (user != null && user.getCurrentPersonaId() != null) {
                            String displayName = "Anonymous";
                            for (com.m3.veilchat.models.Persona persona : user.getPersonas()) {
                                if (persona.getPersonaId().equals(user.getCurrentPersonaId())) {
                                    displayName = persona.getDisplayName();
                                    break;
                                }
                            }

                            Confession confession = new Confession(content, user.getCurrentPersonaId(), displayName);
                            confession.setMoodTag(moodTag);

                            firestore.collection("confessions")
                                    .add(confession)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d(TAG, "Confession posted successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to post confession", e);
                                        errorMessage.setValue("Failed to post confession");
                                    });
                        }
                    }
                });
    }

    // Load all confessions
    public void loadConfessions() {
        firestore.collection("confessions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for confessions", error);
                        errorMessage.setValue("Failed to load confessions");
                        return;
                    }

                    List<Confession> confessionList = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Confession confession = doc.toObject(Confession.class);
                            if (confession != null) {
                                confession.setConfessionId(doc.getId());
                                confessionList.add(confession);
                            }
                        }
                    }
                    confessions.setValue(confessionList);
                });
    }

    // Reply to confession (starts private chat)
    public void replyToConfession(String confessionId, String initialMessage) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        // Get confessor's persona ID
        firestore.collection("confessions").document(confessionId).get()
                .addOnSuccessListener(confessionDoc -> {
                    if (confessionDoc.exists()) {
                        Confession confession = confessionDoc.toObject(Confession.class);
                        if (confession != null) {
                            // Create a private chat between users
                            createPrivateChat(confession.getConfessorPersonaId(), initialMessage);
                        }
                    }
                });
    }

    private void createPrivateChat(String confessorPersonaId, String initialMessage) {
        // Implementation for creating private chat
        // This would create a new private room and send the initial message
        Log.d(TAG, "Creating private chat with confessor");
    }

    // Like a confession
    public void likeConfession(String confessionId) {
        firestore.collection("confessions").document(confessionId)
                .update("likeCount", FieldValue.increment(1))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to like confession", e);
                });
    }

    private String getCurrentUserId() {
        return firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
    }

    // LiveData Getters
    public MutableLiveData<List<Confession>> getConfessions() { return confessions; }
    public MutableLiveData<String> getErrorMessage() { return errorMessage; }
}