package com.example.vocabularylearner.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularylearner.R;
import com.example.vocabularylearner.db.WordDbHelper;
import com.example.vocabularylearner.entity.Word;
import com.example.vocabularylearner.ui.adapter.WordAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordListFragment extends Fragment {
    private static final String ARG_LETTER = "letter";
    private static final String ARG_FAMILIAR = "familiar";

    private RecyclerView recyclerView;
    private WordAdapter wordAdapter;
    private WordDbHelper dbHelper;
    private String currentLetter;
    private boolean isFamiliar;
    private boolean isChineseVisible = true;
    private ExecutorService executorService;

    public static WordListFragment newInstance(String letter, boolean isFamiliar) {
        WordListFragment fragment = new WordListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LETTER, letter);
        args.putBoolean(ARG_FAMILIAR, isFamiliar);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentLetter = getArguments().getString(ARG_LETTER);
            isFamiliar = getArguments().getBoolean(ARG_FAMILIAR);
        }
        dbHelper = new WordDbHelper(requireContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_word_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_words);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadWordsByLetterAndFamiliarity(currentLetter, isFamiliar);
    }

    private void loadWordsByLetterAndFamiliarity(String letter, boolean familiar) {
        executorService.execute(() -> {
            List<Word> words = dbHelper.getWordsByLetterAndFamiliarity(letter, familiar);

            requireActivity().runOnUiThread(() -> {
                if (words.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "没有找到" + (familiar ? "已熟悉" : "未熟悉") + "的单词",
                            Toast.LENGTH_SHORT).show();
                }

                wordAdapter = new WordAdapter(words, isChineseVisible);
                recyclerView.setAdapter(wordAdapter);

                // 设置熟悉度变化监听器
                wordAdapter.setFamiliarityChangeListener(new WordAdapter.OnFamiliarityChangeListener() {
                    @Override
                    public void onFamiliarityChange(Word word, boolean newFamiliar) {
                        executorService.execute(() -> {
                            dbHelper.updateWordFamiliarity(word.getId(), newFamiliar);
                            // 更新后刷新列表
                            requireActivity().runOnUiThread(() ->
                                    loadWordsByLetterAndFamiliarity(currentLetter, isFamiliar));
                        });
                    }

                    @Override
                    public void onFavoriteChange(Word word, boolean isFavorite) {
                        executorService.execute(() -> {
                            dbHelper.updateWordFavorite(word.getId(), isFavorite);
                        });
                    }
                });

                // 设置单词点击监听器
//                wordAdapter.setOnItemClickListener(word -> {
//                    Intent intent = new Intent(requireContext(), WordDetailActivity.class);
//                    intent.putExtra("word_id", word.getId());
//                    startActivity(intent);
//                });
                // 设置单词点击监听器
                // 在loadWordsByLetterAndFamiliarity方法中替换原有的点击监听器部分
                // 设置单词点击监听器
//                wordAdapter.setOnItemClickListener(word -> {
//                    String baseUrl = "https://fanyi.baidu.com/m/trans?aldtype=85&from=en&to=zh&query=";
//                    String url = baseUrl + word.getEnglish(); // 使用单词替换query参数值
//
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(Uri.parse(url));
//                    startActivity(intent);
//                });
                // 替换现有的点击监听器代码
                wordAdapter.setOnItemClickListener(word -> {
                    String baseUrl = "https://fanyi.baidu.com/m/trans?aldtype=85&from=en&to=zh&query=";
                    String url = baseUrl + word.getEnglish(); // 使用单词替换query参数值

                    Intent intent = new Intent(requireContext(), WebViewActivity.class);
                    intent.putExtra("url", url);
                    startActivity(intent);
                });
            });
        });
    }

    public void setChineseVisible(boolean visible) {
        isChineseVisible = visible;
        if (wordAdapter != null) {
            wordAdapter.setChineseVisible(visible);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
        executorService.shutdown();
    }
}