package com.m3.veilchat.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.m3.veilchat.R;
import com.m3.veilchat.models.Message;
import com.m3.veilchat.utils.EncryptionUtils;
import com.m3.veilchat.utils.SecurePrefsManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SYSTEM = 0;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private static final int TYPE_BLINK_SENT = 3;
    private static final int TYPE_BLINK_RECEIVED = 4;
    private static final int TYPE_CIPHER_SENT = 5;
    private static final int TYPE_CIPHER_RECEIVED = 6;

    private List<Message> messages = new ArrayList<>();
    private String currentUserId;
    private String currentRoomId;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    private SecurePrefsManager securePrefsManager;
    private OnMessageClickListener messageClickListener;
    private OnMessageLongClickListener messageLongClickListener;

    // Daily username styling
    private Random random = new Random();
    private int[] usernameColors = {
            Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4"), Color.parseColor("#45B7D1"),
            Color.parseColor("#96CEB4"), Color.parseColor("#FFEAA7"), Color.parseColor("#DDA0DD"),
            Color.parseColor("#98D8C8"), Color.parseColor("#F7DC6F"), Color.parseColor("#BB8FCE")
    };
    private String[] usernameFonts = {"sans-serif", "sans-serif-medium", "sans-serif-light", "serif", "monospace"};

    public MessageAdapter(String currentUserId, String currentRoomId, SecurePrefsManager securePrefsManager) {
        this.currentUserId = currentUserId;
        this.currentRoomId = currentRoomId;
        this.securePrefsManager = securePrefsManager;
    }

    public void setMessages(List<Message> messages) {
        // Filter out expired messages and system messages that are too old
        List<Message> validMessages = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        if (messages != null) {
            for (Message message : messages) {
                // Skip expired messages
                if (message.hasExpired()) {
                    continue;
                }

                // Skip system messages older than 1 hour
                if (message.isSystemMessage() && (currentTime - message.getTimestamp() > 3600000)) {
                    continue;
                }

                validMessages.add(message);
            }
        }

        this.messages = validMessages;
        notifyDataSetChanged();

        // Start blink message countdown if any blink messages exist
        startBlinkMessageCleanup();
    }

    public void addMessage(Message message) {
        if (message != null && !message.hasExpired()) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);

            if (message.isBlinkMessage()) {
                startBlinkMessageCleanup();
            }
        }
    }

    public void updateMessageReadStatus(String messageId, boolean isRead) {
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message.getMessageId().equals(messageId)) {
                message.setRead(isRead);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void deleteMessage(String messageId) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessageId().equals(messageId)) {
                messages.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        if (message.isSystemMessage()) {
            return TYPE_SYSTEM;
        }

        boolean isSent = message.getSenderId().equals(currentUserId);
        String rules = message.getRules();

        if ("blink".equals(rules)) {
            return isSent ? TYPE_BLINK_SENT : TYPE_BLINK_RECEIVED;
        } else if ("cipher".equals(rules)) {
            return isSent ? TYPE_CIPHER_SENT : TYPE_CIPHER_RECEIVED;
        } else {
            return isSent ? TYPE_SENT : TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TYPE_SYSTEM:
                View systemView = inflater.inflate(R.layout.item_message_system, parent, false);
                return new SystemMessageViewHolder(systemView);

            case TYPE_SENT:
                View sentView = inflater.inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageViewHolder(sentView);

            case TYPE_RECEIVED:
                View receivedView = inflater.inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(receivedView);

            case TYPE_BLINK_SENT:
                View blinkSentView = inflater.inflate(R.layout.item_message_blink_sent, parent, false);
                return new BlinkSentMessageViewHolder(blinkSentView);

            case TYPE_BLINK_RECEIVED:
                View blinkReceivedView = inflater.inflate(R.layout.item_message_blink_received, parent, false);
                return new BlinkReceivedMessageViewHolder(blinkReceivedView);

            case TYPE_CIPHER_SENT:
                View cipherSentView = inflater.inflate(R.layout.item_message_cipher_sent, parent, false);
                return new CipherSentMessageViewHolder(cipherSentView);

            case TYPE_CIPHER_RECEIVED:
                View cipherReceivedView = inflater.inflate(R.layout.item_message_cipher_received, parent, false);
                return new CipherReceivedMessageViewHolder(cipherReceivedView);

            default:
                View defaultView = inflater.inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        switch (holder.getItemViewType()) {
            case TYPE_SYSTEM:
                ((SystemMessageViewHolder) holder).bind(message);
                break;

            case TYPE_SENT:
                ((SentMessageViewHolder) holder).bind(message);
                break;

            case TYPE_RECEIVED:
                ((ReceivedMessageViewHolder) holder).bind(message);
                break;

            case TYPE_BLINK_SENT:
                ((BlinkSentMessageViewHolder) holder).bind(message);
                break;

            case TYPE_BLINK_RECEIVED:
                ((BlinkReceivedMessageViewHolder) holder).bind(message);
                break;

            case TYPE_CIPHER_SENT:
                ((CipherSentMessageViewHolder) holder).bind(message);
                break;

            case TYPE_CIPHER_RECEIVED:
                ((CipherReceivedMessageViewHolder) holder).bind(message);
                break;
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (messageClickListener != null) {
                messageClickListener.onMessageClick(message, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (messageLongClickListener != null) {
                return messageLongClickListener.onMessageLongClick(message, holder.getAdapterPosition());
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void startBlinkMessageCleanup() {
        // Check for blink messages and schedule their removal
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message.isBlinkMessage() && message.getExpiresAt() > 0) {
                long remainingTime = message.getExpiresAt() - System.currentTimeMillis();
                if (remainingTime > 0) {
                    scheduleMessageRemoval(message.getMessageId(), remainingTime);
                }
            }
        }
    }

    private void scheduleMessageRemoval(String messageId, long delay) {
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> {
            deleteMessage(messageId);
        }, delay);
    }

    // ViewHolder Classes

    class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSystemMessage;
        private TextView tvSystemTime;

        public SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSystemMessage = itemView.findViewById(R.id.tvSystemMessage);
            tvSystemTime = itemView.findViewById(R.id.tvSystemTime);
        }

        public void bind(Message message) {
            tvSystemMessage.setText(message.getContent());
            tvSystemTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessageContent;
        private TextView tvMessageTime;
        private TextView tvMessageStatus;
        private View messageStatusIndicator;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvMessageStatus = itemView.findViewById(R.id.tvMessageStatus);
            messageStatusIndicator = itemView.findViewById(R.id.messageStatusIndicator);
        }

        public void bind(Message message) {
            tvMessageContent.setText(message.getContent());
            tvMessageTime.setText(timeFormat.format(new Date(message.getTimestamp())));

            // Message status (read/delivered/sent)
            if (message.isRead()) {
                tvMessageStatus.setText("Read");
                tvMessageStatus.setTextColor(Color.parseColor("#4CAF50"));
                messageStatusIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
            } else {
                tvMessageStatus.setText("Sent");
                tvMessageStatus.setTextColor(Color.parseColor("#757575"));
                messageStatusIndicator.setBackgroundColor(Color.parseColor("#757575"));
            }
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSenderName;
        private TextView tvMessageContent;
        private TextView tvMessageTime;
        private ImageView ivSenderAvatar;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            ivSenderAvatar = itemView.findViewById(R.id.ivSenderAvatar);
        }

        public void bind(Message message) {
            // Apply daily username styling
            String displayName = message.getSenderDisplayName() != null ?
                    message.getSenderDisplayName() : "Anonymous";

            SpannableString styledName = new SpannableString(displayName);
            int color = getDailyUsernameColor(message.getSenderId());
            String fontFamily = getDailyUsernameFont(message.getSenderId());

            styledName.setSpan(new ForegroundColorSpan(color), 0, displayName.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            styledName.setSpan(new StyleSpan(Typeface.BOLD), 0, displayName.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvSenderName.setText(styledName);
            // Note: For font family, you'd need a custom TextView or use Calligraphy in production

            tvMessageContent.setText(message.getContent());
            tvMessageTime.setText(timeFormat.format(new Date(message.getTimestamp())));

            // Set avatar based on persona or mood
            setAvatarForPersona(ivSenderAvatar, message.getSenderPersonaId(), message.getSenderDisplayName());
        }

        private void setAvatarForPersona(ImageView imageView, String personaId, String displayName) {
            // Generate consistent avatar based on persona ID or name
            if (personaId != null && !personaId.isEmpty()) {
                int avatarResId = getAvatarResource(personaId);
                imageView.setImageResource(avatarResId);
            } else if (displayName != null) {
                int avatarResId = getAvatarResource(displayName);
                imageView.setImageResource(avatarResId);
            }
        }

        private int getAvatarResource(String seed) {
            // Generate consistent avatar based on seed string
            int hash = seed.hashCode();
            int index = Math.abs(hash) % 8; // 8 different avatars

            int[] avatarResources = {
                    R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3, R.drawable.avatar_4,
                    R.drawable.avatar_5, R.drawable.avatar_6, R.drawable.avatar_7, R.drawable.avatar_8
            };

            return avatarResources[index];
        }
    }

    class BlinkSentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessageContent;
        private TextView tvMessageTime;
        private TextView tvBlinkTimer;
        private View blinkIndicator;

        public BlinkSentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvBlinkTimer = itemView.findViewById(R.id.tvBlinkTimer);
            blinkIndicator = itemView.findViewById(R.id.blinkIndicator);
        }

        public void bind(Message message) {
            tvMessageContent.setText(message.getContent());
            tvMessageTime.setText(timeFormat.format(new Date(message.getTimestamp())));

            // Set blink message styling
            itemView.setAlpha(0.9f);
            blinkIndicator.setVisibility(View.VISIBLE);

            // Update blink timer
            updateBlinkTimer(message, tvBlinkTimer);
        }
    }

    class BlinkReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSenderName;
        private TextView tvMessageContent;
        private TextView tvMessageTime;
        private TextView tvBlinkTimer;
        private View blinkIndicator;
        private ImageView ivSenderAvatar;

        public BlinkReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvBlinkTimer = itemView.findViewById(R.id.tvBlinkTimer);
            blinkIndicator = itemView.findViewById(R.id.blinkIndicator);
            ivSenderAvatar = itemView.findViewById(R.id.ivSenderAvatar);
        }

        public void bind(Message message) {
            // Apply daily username styling
            String displayName = message.getSenderDisplayName() != null ?
                    message.getSenderDisplayName() : "Anonymous";

            SpannableString styledName = new SpannableString(displayName);
            int color = getDailyUsernameColor(message.getSenderId());

            styledName.setSpan(new ForegroundColorSpan(color), 0, displayName.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            styledName.setSpan(new StyleSpan(Typeface.BOLD), 0, displayName.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvSenderName.setText(styledName);
            tvMessageContent.setText(message.getContent());
            tvMessageTime.setText(timeFormat.format(new Date(message.getTimestamp())));

            // Set blink message styling
            itemView.setAlpha(0.9f);
            blinkIndicator.setVisibility(View.VISIBLE);

            // Update blink timer
            updateBlinkTimer(message, tvBlinkTimer);

            // Set avatar
            setAvatarForPersona(ivSenderAvatar, message.getSenderPersonaId(), message.getSenderDisplayName());
        }
    }

    class CipherSentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessageContent;
        private TextView tvMessageTime;
        private TextView tvCipherLabel;
        private ImageView ivCipherLock;

        public CipherSentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvCipherLabel = itemView.findViewById(R.id.tvCipherLabel);
            ivCipherLock = itemView.findViewById(R.id.ivCipherLock);
        }

        public void bind(Message message) {
            // For sent cipher messages, show the original content (sender sees plain text)
            tvMessageContent.setText(message.getContent());
            tvMessageTime.setText(timeFormat.format(new Date(message.getTimestamp())));

            tvCipherLabel.setText("Cipher Message");
            tvCipherLabel.setVisibility(View.VISIBLE);
            ivCipherLock.setVisibility(View.VISIBLE);

            // Cipher message styling
            itemView.setBackgroundColor(Color.parseColor("#1A1A2E"));
            tvMessageContent.setTextColor(Color.parseColor("#E94560"));
        }
    }

    class CipherReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSenderName;
        private TextView tvMessageContent;
        private TextView tvMessageTime;
        private TextView tvCipherLabel;
        private ImageView ivCipherLock;
        private ImageView ivSenderAvatar;
        private View btnDecrypt;

        public CipherReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvCipherLabel = itemView.findViewById(R.id.tvCipherLabel);
            ivCipherLock = itemView.findViewById(R.id.ivCipherLock);
            ivSenderAvatar = itemView.findViewById(R.id.ivSenderAvatar);
            btnDecrypt = itemView.findViewById(R.id.btnDecrypt);
        }

        public void bind(Message message) {
            // Apply daily username styling
            String displayName = message.getSenderDisplayName() != null ?
                    message.getSenderDisplayName() : "Anonymous";

            SpannableString styledName = new SpannableString(displayName);
            int color = getDailyUsernameColor(message.getSenderId());

            styledName.setSpan(new ForegroundColorSpan(color), 0, displayName.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            styledName.setSpan(new StyleSpan(Typeface.BOLD), 0, displayName.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvSenderName.setText(styledName);

            // For received cipher messages, show emoji representation or "Encrypted Message"
            String cipherKey = securePrefsManager.getCipherKey(currentRoomId);
            if (cipherKey != null) {
                try {
                    String decryptedContent = EncryptionUtils.decryptWithEmojiCipher(message.getContent(), cipherKey);
                    tvMessageContent.setText(decryptedContent);
                    tvCipherLabel.setText("Decrypted");
                    ivCipherLock.setImageResource(R.drawable.ic_lock_open);
                } catch (Exception e) {
                    tvMessageContent.setText("🔒 Encrypted Message");
                    tvCipherLabel.setText("Cipher - Tap to Decrypt");
                    ivCipherLock.setImageResource(R.drawable.ic_lock);
                }
            } else {
                tvMessageContent.setText("🔒 Encrypted Message");
                tvCipherLabel.setText("Cipher - Key Required");
                ivCipherLock.setImageResource(R.drawable.ic_lock);
            }

            tvMessageTime.setText(timeFormat.format(new Date(message.getTimestamp())));

            // Cipher message styling
            itemView.setBackgroundColor(Color.parseColor("#1A1A2E"));
            tvMessageContent.setTextColor(Color.parseColor("#E94560"));

            tvCipherLabel.setVisibility(View.VISIBLE);
            ivCipherLock.setVisibility(View.VISIBLE);

            // Set avatar
            setAvatarForPersona(ivSenderAvatar, message.getSenderPersonaId(), message.getSenderDisplayName());

            // Set decrypt button listener
            btnDecrypt.setOnClickListener(v -> {
                if (messageClickListener != null) {
                    messageClickListener.onCipherDecryptRequest(message, getAdapterPosition());
                }
            });
        }
    }

    // Helper Methods

    private void updateBlinkTimer(Message message, TextView timerView) {
        if (message.isBlinkMessage() && message.getExpiresAt() > 0) {
            long remainingTime = message.getExpiresAt() - System.currentTimeMillis();
            if (remainingTime > 0) {
                int seconds = (int) (remainingTime / 1000);
                timerView.setText(seconds + "s");
                timerView.setVisibility(View.VISIBLE);

                // Schedule next update
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(() -> updateBlinkTimer(message, timerView), 1000);
            } else {
                timerView.setVisibility(View.GONE);
            }
        } else {
            timerView.setVisibility(View.GONE);
        }
    }

    private int getDailyUsernameColor(String userId) {
        // Generate consistent but daily-changing color based on user ID and current date
        long daySeed = System.currentTimeMillis() / (24 * 60 * 60 * 1000); // Change daily
        int hash = (userId + daySeed).hashCode();
        int index = Math.abs(hash) % usernameColors.length;
        return usernameColors[index];
    }

    private String getDailyUsernameFont(String userId) {
        // Generate consistent but daily-changing font based on user ID and current date
        long daySeed = System.currentTimeMillis() / (24 * 60 * 60 * 1000); // Change daily
        int hash = (userId + daySeed).hashCode();
        int index = Math.abs(hash) % usernameFonts.length;
        return usernameFonts[index];
    }

    private void setAvatarForPersona(ImageView imageView, String personaId, String displayName) {
        // Generate consistent avatar based on persona ID or name
        String seed = personaId != null ? personaId : (displayName != null ? displayName : "anonymous");
        int avatarResId = getAvatarResource(seed);
        imageView.setImageResource(avatarResId);
    }

    private int getAvatarResource(String seed) {
        int hash = seed.hashCode();
        int index = Math.abs(hash) % 8;

        int[] avatarResources = {
                R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3, R.drawable.avatar_4,
                R.drawable.avatar_5, R.drawable.avatar_6, R.drawable.avatar_7, R.drawable.avatar_8
        };

        return avatarResources[index];
    }

    // Interface for message click events
    public interface OnMessageClickListener {
        void onMessageClick(Message message, int position);
        void onCipherDecryptRequest(Message message, int position);
    }

    public interface OnMessageLongClickListener {
        boolean onMessageLongClick(Message message, int position);
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.messageClickListener = listener;
    }

    public void setOnMessageLongClickListener(OnMessageLongClickListener listener) {
        this.messageLongClickListener = listener;
    }

    // Public methods for external control
    public void markMessageAsRead(String messageId) {
        updateMessageReadStatus(messageId, true);
    }

    public void refreshMessageStyles() {
        // Force refresh to update daily username styles
        notifyDataSetChanged();
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    public Message getMessageAt(int position) {
        if (position >= 0 && position < messages.size()) {
            return messages.get(position);
        }
        return null;
    }
}