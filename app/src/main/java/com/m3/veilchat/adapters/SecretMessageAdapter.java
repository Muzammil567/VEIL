package com.m3.veilchat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.m3.veilchat.R;
import com.m3.veilchat.models.SecretMessage;
import com.m3.veilchat.utils.EncryptionUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SecretMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private static final int TYPE_LOCKED = 3;
    private static final int TYPE_EXPIRED = 4;

    private List<SecretMessage> secretMessages = new ArrayList<>();
    private String currentUserId;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

    private OnSecretMessageClickListener listener;
    private OnLockClickListener lockClickListener;
    private OnDeleteClickListener deleteClickListener;
    private OnViewClickListener viewClickListener;

    public interface OnSecretMessageClickListener {
        void onSecretMessageClick(SecretMessage message);
    }

    public interface OnLockClickListener {
        void onLockClick(SecretMessage message, boolean shouldLock);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(SecretMessage message);
    }

    public interface OnViewClickListener {
        void onViewClick(SecretMessage message);
    }

    public SecretMessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setSecretMessages(List<SecretMessage> messages) {
        this.secretMessages = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addSecretMessage(SecretMessage message) {
        secretMessages.add(0, message); // Add to top
        notifyItemInserted(0);
    }

    public void removeSecretMessage(String messageId) {
        for (int i = 0; i < secretMessages.size(); i++) {
            if (secretMessages.get(i).getMessageId().equals(messageId)) {
                secretMessages.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void updateSecretMessage(SecretMessage message) {
        for (int i = 0; i < secretMessages.size(); i++) {
            if (secretMessages.get(i).getMessageId().equals(message.getMessageId())) {
                secretMessages.set(i, message);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        SecretMessage message = secretMessages.get(position);

        if (message.hasExpired() || !message.canBeRead()) {
            return TYPE_EXPIRED;
        }

        if (message.isLocked() && message.requiresPassword()) {
            return TYPE_LOCKED;
        }

        if (message.getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TYPE_SENT:
                View sentView = inflater.inflate(R.layout.item_secret_message_sent, parent, false);
                return new SentSecretMessageViewHolder(sentView);
            case TYPE_RECEIVED:
                View receivedView = inflater.inflate(R.layout.item_secret_message_received, parent, false);
                return new ReceivedSecretMessageViewHolder(receivedView);
            case TYPE_LOCKED:
                View lockedView = inflater.inflate(R.layout.item_secret_message_locked, parent, false);
                return new LockedSecretMessageViewHolder(lockedView);
            case TYPE_EXPIRED:
                View expiredView = inflater.inflate(R.layout.item_secret_message_expired, parent, false);
                return new ExpiredSecretMessageViewHolder(expiredView);
            default:
                View defaultView = inflater.inflate(R.layout.item_secret_message_received, parent, false);
                return new ReceivedSecretMessageViewHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SecretMessage message = secretMessages.get(position);

        switch (holder.getItemViewType()) {
            case TYPE_SENT:
                ((SentSecretMessageViewHolder) holder).bind(message);
                break;
            case TYPE_RECEIVED:
                ((ReceivedSecretMessageViewHolder) holder).bind(message);
                break;
            case TYPE_LOCKED:
                ((LockedSecretMessageViewHolder) holder).bind(message);
                break;
            case TYPE_EXPIRED:
                ((ExpiredSecretMessageViewHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return secretMessages.size();
    }

    // Set listeners
    public void setOnSecretMessageClickListener(OnSecretMessageClickListener listener) {
        this.listener = listener;
    }

    public void setOnLockClickListener(OnLockClickListener listener) {
        this.lockClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setOnViewClickListener(OnViewClickListener listener) {
        this.viewClickListener = listener;
    }

    // ViewHolder for sent secret messages
    class SentSecretMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessageContent;
        private TextView tvMessageTime;
        private ImageView ivLock;
        private ImageView ivDelete;
        private View lockIndicator;
        private View selfDestructIndicator;

        public SentSecretMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            ivLock = itemView.findViewById(R.id.ivLock);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            lockIndicator = itemView.findViewById(R.id.lockIndicator);
            selfDestructIndicator = itemView.findViewById(R.id.selfDestructIndicator);
        }

        public void bind(SecretMessage message) {
            // Decrypt content if encrypted
            String displayContent = message.getContent();
            if (message.isEncrypted() && message.getEncryptionKey() != null) {
                try {
                    displayContent = EncryptionUtils.decryptWithEmojiCipher(
                            message.getContent(), message.getEncryptionKey());
                } catch (Exception e) {
                    displayContent = "🔒 Encrypted message";
                }
            }

            tvMessageContent.setText(displayContent);
            tvMessageTime.setText(timeFormat.format(message.getTimestamp()));

            // Lock state
            if (message.isLocked()) {
                ivLock.setImageResource(R.drawable.ic_lock);
                lockIndicator.setVisibility(View.VISIBLE);
            } else {
                ivLock.setImageResource(R.drawable.ic_lock_open);
                lockIndicator.setVisibility(View.GONE);
            }

            // Self-destruct indicator
            if (message.isSelfDestruct()) {
                selfDestructIndicator.setVisibility(View.VISIBLE);
            } else {
                selfDestructIndicator.setVisibility(View.GONE);
            }

            // Read count for read-once messages
            if (message.isReadOnce()) {
                tvMessageTime.setText(message.getReadCount() + "/1 read");
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSecretMessageClick(message);
                }
            });

            ivLock.setOnClickListener(v -> {
                if (lockClickListener != null) {
                    lockClickListener.onLockClick(message, !message.isLocked());
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(message);
                }
            });
        }
    }

    // ViewHolder for received secret messages
    class ReceivedSecretMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessageContent;
        private TextView tvMessageTime;
        private ImageView ivLock;
        private ImageView ivDelete;
        private ImageView ivView;
        private View lockIndicator;
        private View selfDestructIndicator;

        public ReceivedSecretMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            ivLock = itemView.findViewById(R.id.ivLock);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivView = itemView.findViewById(R.id.ivView);
            lockIndicator = itemView.findViewById(R.id.lockIndicator);
            selfDestructIndicator = itemView.findViewById(R.id.selfDestructIndicator);
        }

        public void bind(SecretMessage message) {
            // Handle read-once messages
            if (message.isReadOnce() && message.getReadCount() == 0) {
                // First time viewing - show blurred content
                tvMessageContent.setText("👁️‍🗨️ Tap to view (read-once message)");
                tvMessageContent.setAlpha(0.7f);
                ivView.setVisibility(View.VISIBLE);
            } else {
                // Normal message or already viewed
                String displayContent = message.getContent();
                if (message.isEncrypted() && message.getEncryptionKey() != null) {
                    try {
                        displayContent = EncryptionUtils.decryptWithEmojiCipher(
                                message.getContent(), message.getEncryptionKey());
                    } catch (Exception e) {
                        displayContent = "🔒 Encrypted message";
                    }
                }
                tvMessageContent.setText(displayContent);
                tvMessageContent.setAlpha(1.0f);
                ivView.setVisibility(View.GONE);
            }

            tvMessageTime.setText(timeFormat.format(message.getTimestamp()));

            // Lock state
            if (message.isLocked()) {
                ivLock.setImageResource(R.drawable.ic_lock);
                lockIndicator.setVisibility(View.VISIBLE);
            } else {
                ivLock.setImageResource(R.drawable.ic_lock_open);
                lockIndicator.setVisibility(View.GONE);
            }

            // Self-destruct indicator
            if (message.isSelfDestruct()) {
                selfDestructIndicator.setVisibility(View.VISIBLE);
            } else {
                selfDestructIndicator.setVisibility(View.GONE);
            }

            // Read count for read-once messages
            if (message.isReadOnce()) {
                tvMessageTime.setText(message.getReadCount() + "/1 read");
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (message.isReadOnce() && message.getReadCount() == 0) {
                    // First view of read-once message
                    if (viewClickListener != null) {
                        viewClickListener.onViewClick(message);
                    }
                } else if (listener != null) {
                    listener.onSecretMessageClick(message);
                }
            });

            ivLock.setOnClickListener(v -> {
                if (lockClickListener != null) {
                    lockClickListener.onLockClick(message, !message.isLocked());
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(message);
                }
            });

            ivView.setOnClickListener(v -> {
                if (viewClickListener != null) {
                    viewClickListener.onViewClick(message);
                }
            });
        }
    }

    // ViewHolder for locked secret messages
    class LockedSecretMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvLockedMessage;
        private TextView tvLockedTime;
        private ImageView ivUnlock;
        private ImageView ivDelete;

        public LockedSecretMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLockedMessage = itemView.findViewById(R.id.tvLockedMessage);
            tvLockedTime = itemView.findViewById(R.id.tvLockedTime);
            ivUnlock = itemView.findViewById(R.id.ivUnlock);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(SecretMessage message) {
            tvLockedMessage.setText("🔒 Locked message");
            tvLockedTime.setText(timeFormat.format(message.getTimestamp()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSecretMessageClick(message);
                }
            });

            ivUnlock.setOnClickListener(v -> {
                if (lockClickListener != null) {
                    lockClickListener.onLockClick(message, false); // Unlock
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(message);
                }
            });
        }
    }

    // ViewHolder for expired secret messages
    class ExpiredSecretMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvExpiredMessage;
        private TextView tvExpiredTime;
        private ImageView ivDelete;

        public ExpiredSecretMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpiredMessage = itemView.findViewById(R.id.tvExpiredMessage);
            tvExpiredTime = itemView.findViewById(R.id.tvExpiredTime);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(SecretMessage message) {
            if (message.hasExpired()) {
                tvExpiredMessage.setText("⏰ Expired message");
            } else if (message.isReadOnce() && message.getReadCount() >= 1) {
                tvExpiredMessage.setText("👁️‍🗨️ Message destroyed after reading");
            } else {
                tvExpiredMessage.setText("❌ Unavailable message");
            }

            tvExpiredTime.setText(dateFormat.format(message.getTimestamp()));

            ivDelete.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(message);
                }
            });
        }
    }
}