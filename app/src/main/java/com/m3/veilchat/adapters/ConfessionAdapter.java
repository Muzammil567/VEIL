package com.m3.veilchat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.m3.veilchat.R;
import com.m3.veilchat.models.Confession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfessionAdapter extends RecyclerView.Adapter<ConfessionAdapter.ConfessionViewHolder> {
    private List<Confession> confessions = new ArrayList<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public void setConfessions(List<Confession> confessions) {
        this.confessions = confessions != null ? confessions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConfessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_confession, parent, false);
        return new ConfessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfessionViewHolder holder, int position) {
        Confession confession = confessions.get(position);
        holder.bind(confession);
    }

    @Override
    public int getItemCount() {
        return confessions.size();
    }

    static class ConfessionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvConfessionText;
        private TextView tvTimestamp;
        private TextView tvMood;

        public ConfessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConfessionText = itemView.findViewById(R.id.tvContent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvMood = itemView.findViewById(R.id.tvMood);
        }

        public void bind(Confession confession) {
            tvConfessionText.setText(confession.getContent());
            tvTimestamp.setText(dateFormat.format(confession.getTimestamp()));
            tvMood.setText("Feeling: " + confession.getMood());
        }
    }
}
