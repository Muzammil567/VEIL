package com.m3.veilchat.managers;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.m3.veilchat.utils.SecurePrefsManager;
import java.util.HashMap;
import java.util.Map;

public class TrustManager {
    private static final String TAG = "TrustManager";
    private FirebaseFirestore firestore;
    private SecurePrefsManager securePrefsManager;

    public TrustManager(Context context) {
        firestore = FirebaseFirestore.getInstance();
        securePrefsManager = new SecurePrefsManager(context);
    }

    // Reveal identity to another user
    public void revealIdentity(String targetUserId, String personaId) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return;

        Map<String, Object> revealData = new HashMap<>();
        revealData.put("fromUserId", currentUserId);
        revealData.put("toUserId", targetUserId);
        revealData.put("personaId", personaId);
        revealData.put("timestamp", System.currentTimeMillis());
        revealData.put("status", "pending"); // pending, accepted, rejected

        firestore.collection("identity_reveals")
                .add(revealData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Identity reveal request sent");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send identity reveal", e);
                });
    }

    // Accept identity reveal
    public void acceptIdentityReveal(String revealId, String myPersonaId) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", "accepted");
        updateData.put("acceptedAt", System.currentTimeMillis());

        firestore.collection("identity_reveals").document(revealId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    // Create trust connection
                    createTrustConnection(revealId, myPersonaId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to accept identity reveal", e);
                });
    }

    private void createTrustConnection(String revealId, String myPersonaId) {
        // Get the reveal data first
        firestore.collection("identity_reveals").document(revealId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fromUserId = documentSnapshot.getString("fromUserId");
                        String toUserId = documentSnapshot.getString("toUserId");
                        String theirPersonaId = documentSnapshot.getString("personaId");

                        // Create mutual trust connection
                        Map<String, Object> trustConnection = new HashMap<>();
                        trustConnection.put("user1Id", fromUserId);
                        trustConnection.put("user2Id", toUserId);
                        trustConnection.put("user1PersonaId", theirPersonaId);
                        trustConnection.put("user2PersonaId", myPersonaId);
                        trustConnection.put("trustLevel", 1);
                        trustConnection.put("createdAt", System.currentTimeMillis());

                        firestore.collection("trust_connections")
                                .add(trustConnection)
                                .addOnSuccessListener(docRef -> {
                                    Log.d(TAG, "Trust connection established");
                                    awardTrustBadge(fromUserId);
                                    awardTrustBadge(toUserId);
                                });
                    }
                });
    }

    private void awardTrustBadge(String userId) {
        // Update user's trust badge count
        firestore.collection("users").document(userId)
                .update("trustScore", com.google.firebase.firestore.FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Trust badge awarded to user: " + userId);
                });
    }

    // Check trust level between users
    public void getTrustLevel(String otherUserId, TrustLevelCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return;

        firestore.collection("trust_connections")
                .whereEqualTo("user1Id", currentUserId)
                .whereEqualTo("user2Id", otherUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Map<String, Object> connection = queryDocumentSnapshots.getDocuments().get(0).getData();
                        Long trustLevel = (Long) connection.get("trustLevel");
                        callback.onTrustLevelReceived(trustLevel != null ? trustLevel.intValue() : 0);
                    } else {
                        callback.onTrustLevelReceived(0);
                    }
                });
    }

    public interface TrustLevelCallback {
        void onTrustLevelReceived(int trustLevel);
    }

    private String getCurrentUserId() {
        // Implement based on your auth system
        return null; // Replace with actual user ID
    }
}