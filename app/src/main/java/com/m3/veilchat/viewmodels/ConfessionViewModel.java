package com.m3.veilchat.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import com.m3.veilchat.repositories.ConfessionRepository;
import com.m3.veilchat.models.Confession;
import java.util.List;

public class ConfessionViewModel extends ViewModel {
    private ConfessionRepository confessionRepository;

    private MutableLiveData<List<Confession>> confessions = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ConfessionViewModel() {
        confessionRepository = new ConfessionRepository();
        isLoading.setValue(false);
    }

    public void loadConfessions() {
        isLoading.setValue(true);
        confessionRepository.loadConfessions(new ConfessionRepository.ConfessionListener() {
            @Override
            public void onConfessionsLoaded(List<Confession> confessionList) {
                isLoading.setValue(false);
                confessions.setValue(confessionList);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void postConfession(Confession confession) {
        isLoading.setValue(true);
        confessionRepository.postConfession(confession, new ConfessionRepository.ConfessionPostListener() {
            @Override
            public void onConfessionPosted(String confessionId) {
                isLoading.setValue(false);
                // Reload confessions to show the new one
                loadConfessions();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void likeConfession(String confessionId) {
        confessionRepository.likeConfession(confessionId, new ConfessionRepository.LikeListener() {
            @Override
            public void onLiked() {
                // Refresh the list to update like counts
                loadConfessions();
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public void replyToConfession(String confessionId, String message) {
        confessionRepository.replyToConfession(confessionId, message, new ConfessionRepository.ReplyListener() {
            @Override
            public void onReplied() {
                // Handle successful reply
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    // LiveData Getters
    public MutableLiveData<List<Confession>> getConfessions() { return confessions; }
    public MutableLiveData<Boolean> getIsLoading() { return isLoading; }
    public MutableLiveData<String> getErrorMessage() { return errorMessage; }
}