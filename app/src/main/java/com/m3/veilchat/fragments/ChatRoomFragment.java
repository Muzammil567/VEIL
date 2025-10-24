package com.m3.veilchat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.m3.veilchat.R;
import com.m3.veilchat.adapters.MessageAdapter;
import com.m3.veilchat.databinding.FragmentChatRoomBinding;
import com.m3.veilchat.models.Persona;
import com.m3.veilchat.models.User;
import com.m3.veilchat.utils.EncryptionUtils;
import com.m3.veilchat.utils.SecurePrefsManager;
import com.m3.veilchat.utils.ShareUtils;
import com.m3.veilchat.viewmodels.ChatViewModel;
import com.m3.veilchat.viewmodels.UserViewModel;
import java.util.List;

public class ChatRoomFragment extends Fragment {
    private FragmentChatRoomBinding binding;
    private ChatViewModel chatViewModel;
    private UserViewModel userViewModel;
    private MessageAdapter messageAdapter;
    private String roomId;
    private String roomName;
    private String currentRule = "normal";
    private Persona currentPersona;
    private SecurePrefsManager securePrefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        securePrefsManager = new SecurePrefsManager(requireContext());

        if (getArguments() != null) {
            roomId = getArguments().getString("roomId");
            roomName = getArguments().getString("roomName");
        }

        setHasOptionsMenu(true);
        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        setupMessageRules();

        if (roomId != null) {
            chatViewModel.loadRoomMessages(roomId);
        }
    }

    private void setupMessageRules() {
        binding.messageRulesGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.btnRuleNormal) {
                currentRule = "normal";
            } else if (checkedId == R.id.btnRuleBlink) {
                currentRule = "blink";
            } else if (checkedId == R.id.btnRuleCipher) {
                currentRule = "cipher";
                showCipherOptions();
            } else if (checkedId == R.id.btnRuleEncrypted) {
                currentRule = "encrypted";
                showEncryptionInfo();
            }
        });
    }

    private void showCipherOptions() {
        String cipherKey = securePrefsManager.getCipherKey(roomId);
        if (cipherKey == null) {
            cipherKey = EncryptionUtils.generateCipherKey();
            securePrefsManager.storeCipherKey(roomId, cipherKey);
            showCipherKeyDialog(cipherKey, true);
        } else {
            showCipherKeyDialog(cipherKey, false);
        }
    }

    private void showCipherKeyDialog(String cipherKey, boolean isNew) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cipher_key, null);
        TextView tvKey = dialogView.findViewById(R.id.tvCipherKey);
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        Button btnShare = dialogView.findViewById(R.id.btnShareKey);

        tvKey.setText(cipherKey);
        tvTitle.setText(isNew ? R.string.new_cipher_key_title : R.string.existing_cipher_key_title);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, (d, which) -> {
                    binding.btnRuleNormal.setChecked(true);
                    currentRule = "normal";
                })
                .show();

        btnShare.setOnClickListener(v -> {
            shareCipherKey(cipherKey);
            dialog.dismiss();
        });
    }

    private void shareCipherKey(String cipherKey) {
        String shareText = getString(R.string.share_cipher_key_text, roomName, cipherKey);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_cipher_key_title)));
    }

    private void showEncryptionInfo() {
        Toast.makeText(requireContext(), R.string.encryption_info_toast, Toast.LENGTH_SHORT).show();
    }

    private void sendMessage() {
        String messageContent = binding.etMessage.getText().toString().trim();
        if (!messageContent.isEmpty() && roomId != null && currentPersona != null) {
            chatViewModel.sendMessage(roomId, messageContent, currentRule);
            binding.etMessage.setText("");

            if ("blink".equals(currentRule) || "cipher".equals(currentRule)) {
                binding.btnRuleNormal.setChecked(true);
                currentRule = "normal";
            }
        } else if (currentPersona == null) {
            showCreatePersonaDialog();
        }
    }

    private void setupToolbar() {
        binding.toolbar.setTitle(roomName != null ? roomName : "Chat Room");
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void shareRoom() {
        if (roomId != null && roomName != null) {
            ShareUtils.shareRoomInvite(requireContext(), roomId, roomName);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.chat_room_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_switch_persona) {
            showPersonaSwitchDialog();
            return true;
        } else if (id == R.id.menu_room_info) {
            showRoomInfo();
            return true;
        } else if (id == R.id.menu_leave_room) {
            leaveRoom();
            return true;
        } else if (id == R.id.menu_share_room) {
            shareRoom();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPersonaSwitchDialog() {
        User user = userViewModel.getCurrentUser().getValue();
        if (user == null || user.getPersonas() == null || user.getPersonas().isEmpty()) {
            showCreatePersonaDialog();
            return;
        }

        List<Persona> personas = user.getPersonas();
        String[] personaNames = new String[personas.size()];
        for (int i = 0; i < personas.size(); i++) {
            personaNames[i] = personas.get(i).getDisplayName() + " (" + personas.get(i).getMood() + ")";
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.switch_persona_title)
                .setItems(personaNames, (dialog, which) -> {
                    Persona selectedPersona = personas.get(which);
                    userViewModel.switchPersona(selectedPersona.getPersonaId());
                    currentPersona = selectedPersona;
                    updateCurrentPersonaDisplay();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showCreatePersonaDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.no_personas_title)
                .setMessage(R.string.no_personas_message)
                .setPositiveButton(R.string.create, (dialog, which) -> Navigation.findNavController(requireView()).navigate(R.id.navigation_personas))
                .setNegativeButton(R.string.later, null)
                .show();
    }

    private void updateCurrentPersonaDisplay() {
        if (currentPersona != null) {
            binding.tvCurrentPersona.setText(getString(R.string.current_persona_display, currentPersona.getDisplayName()));
            binding.tvCurrentPersona.setVisibility(View.VISIBLE);
        } else {
            binding.tvCurrentPersona.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        User currentUser = userViewModel.getCurrentUser().getValue();
        String currentUserId = (currentUser != null) ? currentUser.getUserId() : "";
        messageAdapter = new MessageAdapter(currentUserId);
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMessages.setAdapter(messageAdapter);
    }

    private void setupObservers() {
        chatViewModel.getRoomMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                messageAdapter.submitList(messages);
                binding.rvMessages.scrollToPosition(messages.size() - 1);
            }
            binding.tvNoMessages.setVisibility(messages == null || messages.isEmpty() ? View.VISIBLE : View.GONE);
        });

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getPersonas() != null && !user.getPersonas().isEmpty()) {
                if (user.getCurrentPersonaId() != null) {
                    for (Persona persona : user.getPersonas()) {
                        if (persona.getPersonaId().equals(user.getCurrentPersonaId())) {
                            currentPersona = persona;
                            break;
                        }
                    }
                }
                if (currentPersona == null) {
                    currentPersona = user.getPersonas().get(0);
                    userViewModel.switchPersona(currentPersona.getPersonaId());
                }
                updateCurrentPersonaDisplay();
            }
        });

        chatViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                binding.tvError.setText(errorMessage);
                binding.tvError.setVisibility(View.VISIBLE);
            } else {
                binding.tvError.setVisibility(View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnSendMessage.setOnClickListener(v -> sendMessage());
        binding.etMessage.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN) && (keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void showRoomInfo() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.room_info_title)
                .setMessage(getString(R.string.room_info_message, roomName, roomId))
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void leaveRoom() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.leave_room_title)
                .setMessage(R.string.leave_room_message)
                .setPositiveButton(R.string.leave, (dialog, which) -> {
                    if (roomId != null) {
                        chatViewModel.leaveRoom(roomId);
                        Navigation.findNavController(requireView()).navigateUp();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
