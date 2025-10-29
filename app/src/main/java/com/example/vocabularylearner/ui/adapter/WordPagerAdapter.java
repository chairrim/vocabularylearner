package com.example.vocabularylearner.ui.adapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.vocabularylearner.ui.WordListFragment;

import java.util.List;

public class WordPagerAdapter extends FragmentStateAdapter {
    private final String letter;
    private final List<Boolean> familiarityStates;

    public WordPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                            String letter) {
        super(fragmentActivity);
        this.letter = letter;

        // 0: 未熟悉, 1: 已熟悉
        this.familiarityStates = List.of(false, true);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return WordListFragment.newInstance(letter, familiarityStates.get(position));
    }

    @Override
    public int getItemCount() {
        return familiarityStates.size();
    }
}