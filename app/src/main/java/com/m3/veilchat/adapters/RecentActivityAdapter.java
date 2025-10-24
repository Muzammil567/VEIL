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
import com.m3.veilchat.models.RecentActivity;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RecentActivityAdapter extends ListAdapter<RecentActivity, RecentActivityAdapter.ActivityViewHolder> {
    private OnActivityClickListener listener;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public interface OnActivityClickListener {
        void onActivityClick(RecentActivity activity);
    }

    public RecentActivityAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnActivityClickListener(OnActivityClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        RecentActivity activity = getItem(position);
        holder.bind(activity);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActivityClick(activity);
            }
        });
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvActivityDescription;
        private final TextView tvRoomName;
        private final TextView tvTimestamp;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivityDescription = itemView.findViewById(R.id.tvActivityDescription);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        public void bind(RecentActivity activity) {
            tvActivityDescription.setText(activity.getDescription());
            tvRoomName.setText(activity.getRoomName());
            tvTimestamp.setText(dateFormat.format(activity.getTimestamp()));
        }
    }

    private static final DiffUtil.ItemCallback<RecentActivity> DIFF_CALLBACK = new DiffUtil.ItemCallback<RecentActivity>() {
        @Override
        public boolean areItemsTheSame(@NonNull RecentActivity oldItem, @NonNull RecentActivity newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull RecentActivity oldItem, @NonNull RecentActivity newItem) {
            return oldItem.equals(newItem);
        }
    };
}
