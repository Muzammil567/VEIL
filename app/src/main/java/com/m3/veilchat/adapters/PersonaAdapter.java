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
import com.m3.veilchat.models.Persona;

public class PersonaAdapter extends ListAdapter<Persona, PersonaAdapter.PersonaViewHolder> {
    private OnPersonaClickListener listener;

    public interface OnPersonaClickListener {
        void onPersonaClick(Persona persona);
        void onPersonaSwitch(Persona persona);
    }

    public PersonaAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnPersonaClickListener(OnPersonaClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PersonaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_persona, parent, false);
        return new PersonaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonaViewHolder holder, int position) {
        Persona persona = getItem(position);
        holder.bind(persona);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPersonaClick(persona);
            }
        });

        holder.btnSwitch.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPersonaSwitch(persona);
            }
        });
    }

    static class PersonaViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPersonaName;
        private final TextView tvMood;
        private final TextView tvInterest;
        private final View btnSwitch;

        public PersonaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPersonaName = itemView.findViewById(R.id.tvPersonaName);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvInterest = itemView.findViewById(R.id.tvInterest);
            btnSwitch = itemView.findViewById(R.id.btnSwitch);
        }

        public void bind(Persona persona) {
            tvPersonaName.setText(persona.getDisplayName());
            tvMood.setText("Mood: " + (persona.getMood() != null ? persona.getMood() : "Neutral"));
            tvInterest.setText("Interest: " + (persona.getInterestTag() != null ? persona.getInterestTag() : "#general"));
        }
    }

    private static final DiffUtil.ItemCallback<Persona> DIFF_CALLBACK = new DiffUtil.ItemCallback<Persona>() {
        @Override
        public boolean areItemsTheSame(@NonNull Persona oldItem, @NonNull Persona newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Persona oldItem, @NonNull Persona newItem) {
            return oldItem.equals(newItem);
        }
    };
}
