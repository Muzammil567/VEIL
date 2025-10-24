package com.m3.veilchat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.m3.veilchat.R;
import com.m3.veilchat.models.ChatRoom;

public class ChatRoomAdapter extends ListAdapter<ChatRoom, ChatRoomAdapter.ChatRoomViewHolder> {
    private OnChatRoomClickListener listener;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
        void onChatRoomJoin(ChatRoom chatRoom);
    }

    public ChatRoomAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnChatRoomClickListener(OnChatRoomClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom room = getItem(position);
        holder.bind(room);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatRoomClick(room);
            }
        });

        holder.btnJoin.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatRoomJoin(room);
            }
        });
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoomName;
        private final TextView tvRoomDescription;
        private final TextView tvRoomInfo;
        private final TextView tvParticipantCount;
        private final View btnJoin;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomDescription = itemView.findViewById(R.id.tvRoomDescription);
            tvRoomInfo = itemView.findViewById(R.id.tvRoomInfo);
            tvParticipantCount = itemView.findViewById(R.id.tvParticipantCount);
            btnJoin = itemView.findViewById(R.id.btnJoin);
        }

        public void bind(ChatRoom room) {
            tvRoomName.setText(room.getName());
            tvRoomDescription.setText(room.getDescription());

            StringBuilder info = new StringBuilder();
            info.append(room.getRoomType().substring(0, 1).toUpperCase())
                    .append(room.getRoomType().substring(1));

            if (room.getMoodTag() != null) {
                info.append(" • ").append(room.getMoodTag());
            }
            if (room.getInterestTag() != null) {
                info.append(" • ").append(room.getInterestTag());
            }

            tvRoomInfo.setText(info.toString());

            int participantCount = room.getParticipantIds() != null ? room.getParticipantIds().size() : 0;
            tvParticipantCount.setText(itemView.getContext().getResources().getQuantityString(R.plurals.participant_count, participantCount, participantCount));

            btnJoin.setVisibility(View.VISIBLE);
        }
    }

    private static final DiffUtil.ItemCallback<ChatRoom> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChatRoom>() {
        @Override
        public boolean areItemsTheSame(@NonNull ChatRoom oldItem, @NonNull ChatRoom newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChatRoom oldItem, @NonNull ChatRoom newItem) {
            return oldItem.equals(newItem);
        }
    };
}
