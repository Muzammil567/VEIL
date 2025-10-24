package com.m3.veilchat.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.m3.veilchat.models.Persona;
import com.m3.veilchat.models.User;
import java.util.HashMap;
import java.util.Map;

public class UserViewModel extends ViewModel {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public UserViewModel() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        isLoading.setValue(false);
        loadUserData();
    }

    private void loadUserData() {
        if (firebaseAuth.getCurrentUser() != null) {
            String uid = firebaseAuth.getCurrentUser().getUid();
            firestore.collection("users").document(uid)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            errorMessage.setValue("Failed to load user data");
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                user.setUserId(uid);
                                currentUser.setValue(user);
                            }
                        } else {
                            errorMessage.setValue("User data not found");
                        }
                    });
        }
    }

    public void addPersona(Persona persona) {
        User user = currentUser.getValue();
        if (user == null || getCurrentUserId() == null) return;

        String personaId = "persona_" + System.currentTimeMillis();
        persona.setPersonaId(personaId);

        if (user.getPersonas() == null) {
            user.setPersonas(new java.util.ArrayList<>());
        }

        user.getPersonas().add(persona);

        if (user.getPersonas().size() == 1) {
            user.setCurrentPersonaId(personaId);
        }

        updateUserInFirestore(user);
    }

    public void switchPersona(String personaId) {
        User user = currentUser.getValue();
        if (user == null || getCurrentUserId() == null) return;

        user.setCurrentPersonaId(personaId);
        updateUserInFirestore(user);
    }

    private void updateUserInFirestore(User user) {
        String uid = getCurrentUserId();
        if (uid == null) return;

        firestore.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> currentUser.setValue(user))
                .addOnFailureListener(e -> errorMessage.setValue("Failed to update user"));
    }

    public void incrementConfessionCount() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        firestore.collection("users").document(userId)
                .update("totalConfessions", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> loadUserData())
                .addOnFailureListener(e -> {});
    }

    private String getCurrentUserId() {
        return firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
