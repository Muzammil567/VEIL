package com.m3.veilchat.fragments.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.m3.veilchat.R;
import com.m3.veilchat.adapters.MoodAdapter;
import com.m3.veilchat.models.Confession;
import com.m3.veilchat.models.Mood;
import java.util.ArrayList;
import java.util.List;

public class AddConfessionDialog extends DialogFragment {
    private TextInputEditText etConfessionContent;
    private RecyclerView rvMoods;
    private TextView tvSelectedMood;
    private MaterialButtonToggleGroup toggleCategory;
    private MaterialButton btnPostConfession;
    private MaterialButton btnCancel;

    private MoodAdapter moodAdapter;
    private OnConfessionPostedListener listener;
    private String selectedCategory = "Secret";
    private Mood selectedMood;

    private final List<Mood> availableMoods = new ArrayList<Mood>() {{
        add(new Mood("😊", "Happy", "Feeling joyful and content"));
        add(new Mood("😔", "Sad", "Feeling down or unhappy"));
        add(new Mood("😡", "Angry", "Feeling frustrated or mad"));
        add(new Mood("😢", "Regret", "Wishing things were different"));
        add(new Mood("❤️", "Love", "Feeling affectionate or romantic"));
        add(new Mood("🤔", "Confused", "Unsure or puzzled"));
        add(new Mood("😰", "Anxious", "Feeling worried or nervous"));
        add(new Mood("🎉", "Excited", "Feeling enthusiastic and eager"));
        add(new Mood("😴", "Tired", "Feeling exhausted or drained"));
        add(new Mood("🤩", "Hopeful", "Optimistic about the future"));
    }};

    public interface OnConfessionPostedListener {
        void onConfessionPosted(Confession confession);
    }

    public void setOnConfessionPostedListener(OnConfessionPostedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setView(createView())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return createView();
    }

    private View createView() {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_confession, container, false);

        initViews(view);
        setupMoods();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        etConfessionContent = view.findViewById(R.id.etConfessionContent);
        rvMoods = view.findViewById(R.id.rvMoods);
        tvSelectedMood = view.findViewById(R.id.tvSelectedMood);
        toggleCategory = view.findViewById(R.id.toggleCategory);
        btnPostConfession = view.findViewById(R.id.btnPostConfession);
        btnCancel = view.findViewById(R.id.btnCancel);

        // Setup moods recycler view
        rvMoods.setLayoutManager(new GridLayoutManager(requireContext(), 5));
        moodAdapter = new MoodAdapter();
        rvMoods.setAdapter(moodAdapter);
    }

    private void setupMoods() {
        moodAdapter.setMoods(availableMoods);
        moodAdapter.setOnMoodClickListener(new MoodAdapter.OnMoodClickListener() {
            @Override
            public void onMoodClick(Mood mood, int position) {
                selectedMood = mood;
                tvSelectedMood.setText("Selected: " + mood.getEmoji() + " " + mood.getDisplayName());
                tvSelectedMood.setVisibility(View.VISIBLE);
            }
        });

        // Select first mood by default
        if (!availableMoods.isEmpty()) {
            moodAdapter.setSelectedPosition(0);
            selectedMood = availableMoods.get(0);
            tvSelectedMood.setText("Selected: " + selectedMood.getEmoji() + " " + selectedMood.getDisplayName());
            tvSelectedMood.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> {
            dismiss();
        });

        btnPostConfession.setOnClickListener(v -> {
            postConfession();
        });

        // Category selection
        toggleCategory.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnCategoryLove) {
                    selectedCategory = "Love";
                } else if (checkedId == R.id.btnCategoryFriendship) {
                    selectedCategory = "Friendship";
                } else if (checkedId == R.id.btnCategoryFamily) {
                    selectedCategory = "Family";
                } else if (checkedId == R.id.btnCategoryWork) {
                    selectedCategory = "Work";
                } else if (checkedId == R.id.btnCategorySecret) {
                    selectedCategory = "Secret";
                } else if (checkedId == R.id.btnCategoryHope) {
                    selectedCategory = "Hope";
                }
            }
        });

        // Set default category
        MaterialButton defaultCategory = requireView().findViewById(R.id.btnCategorySecret);
        if (defaultCategory != null) {
            defaultCategory.setChecked(true);
        }
    }

    private void postConfession() {
        String content = etConfessionContent.getText().toString().trim();

        if (content.isEmpty()) {
            etConfessionContent.setError("Please write your confession");
            return;
        }

        if (content.length() > 500) {
            etConfessionContent.setError("Confession too long (max 500 characters)");
            return;
        }

        if (selectedMood == null) {
            // Show error for mood selection
            return;
        }

        // Create confession object
        Confession confession = new Confession();
        confession.setContent(content);
        confession.setMood(selectedMood.getDisplayName());
        confession.setCategory(selectedCategory);
        confession.setAnonymous(true);

        if (listener != null) {
            listener.onConfessionPosted(confession);
        }

        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}