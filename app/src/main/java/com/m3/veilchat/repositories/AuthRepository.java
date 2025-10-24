package com.m3.veilchat.repositories;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.m3.veilchat.models.User;
import java.util.UUID;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        isLoading.setValue(false);

        // Check if user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            loadUserData(firebaseAuth.getCurrentUser().getUid());
        }
    }

    // Anonymous Sign In
    public void signInAnonymously() {
        isLoading.setValue(true);
        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            createNewUser(firebaseUser.getUid());
                        }
                    } else {
                        errorMessage.setValue("Failed to create anonymous account: " +
                                task.getException().getMessage());
                        Log.e(TAG, "Anonymous sign in failed", task.getException());
                    }
                });
    }

    // Create new user in Firestore
    private void createNewUser(String uid) {
        String anonymousId = generateAnonymousId();
        String username = generateUniqueUsername();

        User newUser = new User(anonymousId, username);
        newUser.setUserId(uid);

        firestore.collection("users").document(uid)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User created successfully");
                    userLiveData.setValue(newUser);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create user", e);
                    errorMessage.setValue("Failed to create user profile");
                });
    }

    // Link email to existing anonymous account
    public void linkEmail(String email, String password) {
        if (firebaseAuth.getCurrentUser() == null) {
            errorMessage.setValue("No user logged in");
            return;
        }

        isLoading.setValue(true);
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        firebaseAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email linked successfully");
                        // Update user in Firestore with email
                        updateUserEmail(email);
                    } else {
                        errorMessage.setValue("Failed to link email: " +
                                task.getException().getMessage());
                        Log.e(TAG, "Email linking failed", task.getException());
                    }
                });
    }

    // Sign in with email (for returning users who linked email)
    public void signInWithEmail(String email, String password) {
        isLoading.setValue(true);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            loadUserData(firebaseUser.getUid());
                        }
                    } else {
                        errorMessage.setValue("Sign in failed: " +
                                task.getException().getMessage());
                    }
                });
    }

    private void updateUserEmail(String email) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        firestore.collection("users").document(uid)
                .update("email", email, "emailVerified", true)
                .addOnSuccessListener(aVoid -> {
                    // Reload user data
                    loadUserData(uid);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to update user email");
                });
    }

    private void loadUserData(String uid) {
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        userLiveData.setValue(user);
                    } else {
                        errorMessage.setValue("User data not found");
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to load user data");
                });
    }

    // Helper methods
    private String generateAnonymousId() {
        return "anon_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateUniqueUsername() {
        String[] adjectives = {"Mysterious", "Silent", "Hidden", "Secret", "Quiet", "Shadow", "Ghost"};
        String[] nouns = {"Stranger", "Traveler", "Whisper", "Visitor", "Soul", "Spirit", "Mask"};

        String adj = adjectives[(int) (Math.random() * adjectives.length)];
        String noun = nouns[(int) (Math.random() * nouns.length)];
        int number = (int) (Math.random() * 999);

        return adj + noun + number;
    }

    // Getters for LiveData
    public MutableLiveData<User> getUserLiveData() { return userLiveData; }
    public MutableLiveData<String> getErrorMessage() { return errorMessage; }
    public MutableLiveData<Boolean> getIsLoading() { return isLoading; }

    // Check if user is logged in
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    // Sign out
    public void signOut() {
        firebaseAuth.signOut();
        userLiveData.setValue(null);
    }
}