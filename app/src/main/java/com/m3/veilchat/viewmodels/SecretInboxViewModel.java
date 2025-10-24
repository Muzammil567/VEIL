package com.m3.veilchat.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import com.m3.veilchat.models.SecretMessage;
import com.m3.veilchat.repositories.SecretInboxRepository;
import java.util.List;

public class SecretInboxViewModel extends ViewModel {
    private SecretInboxRepository secretInboxRepository;

    private MutableLiveData<List<SecretMessage>> secretMessages = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SecretInboxViewModel() {
        secretInboxRepository = new SecretInboxRepository();
        isLoading.setValue(false);
    }

    public void loadSecretMessages() {
        isLoading.setValue(true);
        secretInboxRepository.loadSecretMessages(new SecretInboxRepository.SecretMessageListener() {
            @Override
            public void onSecretMessagesLoaded(List<SecretMessage> messages) {
                isLoading.setValue(false);
                secretMessages.setValue(messages);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void addSecretMessage(SecretMessage message) {
        secretInboxRepository.addSecretMessage(message, new SecretInboxRepository.SecretMessageOperationListener() {
            @Override
            public void onSuccess() {
                loadSecretMessages(); // Reload to get updated list
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public void updateSecretMessage(SecretMessage message) {
        secretInboxRepository.updateSecretMessage(message, new SecretInboxRepository.SecretMessageOperationListener() {
            @Override
            public void onSuccess() {
                loadSecretMessages();
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public void deleteSecretMessage(String messageId) {
        secretInboxRepository.deleteSecretMessage(messageId, new SecretInboxRepository.SecretMessageOperationListener() {
            @Override
            public void onSuccess() {
                loadSecretMessages();
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    // LiveData Getters
    public MutableLiveData<List<SecretMessage>> getSecretMessages() { return secretMessages; }
    public MutableLiveData<Boolean> getIsLoading() { return isLoading; }
    public MutableLiveData<String> getErrorMessage() { return errorMessage; }
}