package com.m3.veilchat.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.m3.veilchat.databinding.ActivityVoiceCallBinding;
import com.m3.veilchat.utils.SecurePrefsManager;

import java.io.File;
import java.io.IOException;

public class VoiceCallActivity extends AppCompatActivity {
    private static final String TAG = "VoiceCallActivity";
    private static final int RECORD_AUDIO_PERMISSION = 101;

    private ActivityVoiceCallBinding binding;
    private SecurePrefsManager securePrefsManager;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String outputFile;
    private boolean isRecording = false;
    private String callType; // "outgoing" or "incoming"
    private Handler callHandler = new Handler(Looper.getMainLooper());
    private Runnable callRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVoiceCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        securePrefsManager = new SecurePrefsManager(this);

        // Get call details
        String roomName = getIntent().getStringExtra("roomName");
        callType = getIntent().getStringExtra("callType");

        setupUI(roomName);
        setupClickListeners();
        checkPermissions();

        if ("outgoing".equals(callType)) {
            startOutgoingCall();
        } else {
            showIncomingCall();
        }
    }

    private void setupUI(String roomName) {
        binding.tvCallStatus.setText("Connecting...");
        binding.tvRoomName.setText(roomName != null ? roomName : "Voice Call");

        binding.chronometer.setBase(System.currentTimeMillis());
        binding.chronometer.setFormat("Call Time: %s");

        if ("outgoing".equals(callType)) {
            binding.tvCallType.setText("Outgoing Call");
            binding.btnAcceptCall.setVisibility(View.GONE);
            binding.btnDeclineCall.setText("Cancel");
        } else {
            binding.tvCallType.setText("Incoming Call");
            binding.btnAcceptCall.setVisibility(View.VISIBLE);
            binding.btnDeclineCall.setText("Decline");
        }
    }

    private void setupClickListeners() {
        binding.btnAcceptCall.setOnClickListener(v -> acceptCall());
        binding.btnDeclineCall.setOnClickListener(v -> endCall());
        binding.btnMute.setOnClickListener(v -> toggleMute());
        binding.btnSpeaker.setOnClickListener(v -> toggleSpeaker());
        binding.btnRecord.setOnClickListener(v -> toggleRecording());
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION);
        } else {
            initializeAudio();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeAudio();
            } else {
                binding.tvCallStatus.setText("Microphone permission required");
            }
        }
    }

    private void initializeAudio() {
        outputFile = getExternalCacheDir().getAbsolutePath() + "/voice_call_recording.3gp";
        setupMediaRecorder();
    }

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(outputFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "MediaRecorder prepare failed", e);
        }
    }

    private void startOutgoingCall() {
        binding.tvCallStatus.setText("Calling...");
        callRunnable = () -> {
            binding.tvCallStatus.setText("Call Connected");
            startCallTimer();
            showCallControls();
        };
        callHandler.postDelayed(callRunnable, 3000);
    }

    private void showIncomingCall() {
        binding.tvCallStatus.setText("Incoming Call");
        playRingtone();
    }

    private void acceptCall() {
        stopRingtone();
        binding.tvCallStatus.setText("Call Connected");
        binding.btnAcceptCall.setVisibility(View.GONE);
        binding.btnDeclineCall.setText("End Call");
        startCallTimer();
        showCallControls();
    }

    private void endCall() {
        binding.tvCallStatus.setText("Call Ended");

        if (isRecording) {
            stopRecording();
        }

        stopCallTimer();

        if (securePrefsManager.isVanishCallsEnabled()) {
            deleteCallRecording();
        }

        callHandler.postDelayed(this::finish, 2000);
    }

    private void startCallTimer() {
        binding.chronometer.setBase(System.currentTimeMillis());
        binding.chronometer.start();
    }

    private void stopCallTimer() {
        binding.chronometer.stop();
    }

    private void showCallControls() {
        binding.callControls.setVisibility(View.VISIBLE);
    }

    private void toggleMute() {
        boolean isMuted = !binding.btnMute.isSelected();
        binding.btnMute.setSelected(isMuted);
        binding.btnMute.setText(isMuted ? "Unmute" : "Mute");
    }

    private void toggleSpeaker() {
        boolean isSpeakerOn = !binding.btnSpeaker.isSelected();
        binding.btnSpeaker.setSelected(isSpeakerOn);
        binding.btnSpeaker.setText(isSpeakerOn ? "Earpiece" : "Speaker");
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        try {
            mediaRecorder.start();
            isRecording = true;
            binding.btnRecord.setText("Stop Recording");
            binding.tvRecordingStatus.setText("Recording...");
            binding.tvRecordingStatus.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "Recording start failed", e);
        }
    }

    private void stopRecording() {
        try {
            mediaRecorder.stop();
            mediaRecorder.reset();
            isRecording = false;
            binding.btnRecord.setText("Record Call");
            binding.tvRecordingStatus.setText("Recording Saved");
        } catch (Exception e) {
            Log.e(TAG, "Recording stop failed", e);
        }
    }

    private void deleteCallRecording() {
        File file = new File(outputFile);
        if (file.exists()) {
            if (file.delete()) {
                Log.d(TAG, "Call recording deleted (vanish mode)");
            } else {
                Log.e(TAG, "Failed to delete call recording");
            }
        }
    }

    private void playRingtone() {
        try {
            mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_RINGTONE_URI);
            if(mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ringtone play failed", e);
        }
    }

    private void stopRingtone() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }

        stopRingtone();

        if (callHandler != null && callRunnable != null) {
            callHandler.removeCallbacks(callRunnable);
        }
    }
}
