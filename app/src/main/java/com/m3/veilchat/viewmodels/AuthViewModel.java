package com.m3.veilchat.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.m3.veilchat.repositories.AuthRepository;
import com.m3.veilchat.models.User;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    public AuthViewModel() {
        authRepository = new AuthRepository();
    }

    public void signInAnonymously() {
        authRepository.signInAnonymously();
    }

    public void linkEmail(String email, String password) {
        authRepository.linkEmail(email, password);
    }

    public void signInWithEmail(String email, String password) {
        authRepository.signInWithEmail(email, password);
    }

    public void signOut() {
        authRepository.signOut();
    }

    public LiveData<User> getUserLiveData() {
        return authRepository.getUserLiveData();
    }

    public LiveData<String> getErrorMessage() {
        return authRepository.getErrorMessage();
    }

    public LiveData<Boolean> getIsLoading() {
        return authRepository.getIsLoading();
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }
}
