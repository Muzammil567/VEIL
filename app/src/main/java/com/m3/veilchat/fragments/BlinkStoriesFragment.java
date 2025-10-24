package com.m3.veilchat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.m3.veilchat.adapters.BlinkStoriesAdapter;
import com.m3.veilchat.databinding.FragmentBlinkStoriesBinding;
import com.m3.veilchat.viewmodels.BlinkStoriesViewModel;

public class BlinkStoriesFragment extends Fragment {
    private FragmentBlinkStoriesBinding binding;
    private BlinkStoriesViewModel storiesViewModel;
    private BlinkStoriesAdapter storiesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBlinkStoriesBinding.inflate(inflater, container, false);
        storiesViewModel = new ViewModelProvider(this).get(BlinkStoriesViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        storiesViewModel.loadStories();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        storiesAdapter = new BlinkStoriesAdapter();
        binding.rvStories.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvStories.setAdapter(storiesAdapter);

        storiesAdapter.setOnStoryClickListener(story -> {
            showStoryViewer(story);
        });
    }

    private void setupObservers() {
        storiesViewModel.getStories().observe(getViewLifecycleOwner(), stories -> {
            if (stories != null && !stories.isEmpty()) {
                binding.tvNoStories.setVisibility(View.GONE);
                binding.rvStories.setVisibility(View.VISIBLE);
                storiesAdapter.setStories(stories);
            } else {
                binding.tvNoStories.setVisibility(View.VISIBLE);
                binding.rvStories.setVisibility(View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        binding.fabAddStory.setOnClickListener(v -> {
            showAddStoryDialog();
        });
    }

    private void showStoryViewer(BlinkStory story) {
        StoryViewerDialog dialog = new StoryViewerDialog(story);
        dialog.setOnStoryViewedListener(() -> {
            storiesViewModel.markStoryAsViewed(story.getStoryId());
        });
        dialog.show(getParentFragmentManager(), "StoryViewer");
    }

    private void showAddStoryDialog() {
        AddBlinkStoryDialog dialog = new AddBlinkStoryDialog();
        dialog.setOnStoryCreatedListener((content, mood) -> {
            storiesViewModel.createStory(content, mood);
        });
        dialog.show(getParentFragmentManager(), "AddBlinkStory");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}