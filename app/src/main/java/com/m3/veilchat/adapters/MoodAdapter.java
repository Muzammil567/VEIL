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
import com.m3.veilchat.models.Mood;

public class MoodAdapter extends ListAdapter<Mood, MoodAdapter.MoodViewHolder> {
    private OnMoodClickListener listener;
    private int selectedPosition = -1;

    public interface OnMoodClickListener {
        void onMoodClick(Mood mood, int position);
    }

    public MoodAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnMoodClickListener(OnMoodClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;

        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public Mood getSelectedMood() {
        if (selectedPosition >= 0 && selectedPosition < getCurrentList().size()) {
            return getItem(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = getItem(position);
        holder.bind(mood, position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            setSelectedPosition(position);
            if (listener != null) {
                listener.onMoodClick(mood, position);
            }
        });
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMoodEmoji;
        private final TextView tvMoodText;
        private final View moodContainer;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMoodEmoji = itemView.findViewById(R.id.tvMoodEmoji);
            tvMoodText = itemView.findViewById(R.id.tvMoodText);
            moodContainer = itemView.findViewById(R.id.moodContainer);
        }

        public void bind(Mood mood, boolean isSelected) {
            tvMoodEmoji.setText(mood.getEmoji());
            tvMoodText.setText(mood.getDisplayName());

            if (isSelected) {
                moodContainer.setBackgroundResource(R.drawable.mood_selected_background);
                itemView.setElevation(4f);
            } else {
                moodContainer.setBackgroundResource(R.drawable.mood_unselected_background);
                itemView.setElevation(1f);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Mood> DIFF_CALLBACK = new DiffUtil.ItemCallback<Mood>() {
        @Override
        public boolean areItemsTheSame(@NonNull Mood oldItem, @NonNull Mood newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Mood oldItem, @NonNull Mood newItem) {
            return oldItem.equals(newItem);
        }
    };
}
