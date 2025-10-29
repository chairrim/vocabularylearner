package com.example.vocabularylearner.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vocabularylearner.R;
import com.example.vocabularylearner.entity.Word;
import com.example.vocabularylearner.db.WordDbHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordDetailActivity extends AppCompatActivity {
    private TextView tvEnglish, tvPhonetic, tvChinese, tvExample;
    private ImageButton  btnFamiliar, btnBack;
    private WordDbHelper dbHelper;
    private long wordId;
    private Word currentWord;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);

        // 初始化
        dbHelper = new WordDbHelper(this);
        executorService = Executors.newSingleThreadExecutor();
        wordId = getIntent().getLongExtra("word_id", -1);

        if (wordId == -1) {
            finish();
            return;
        }

        // 绑定控件
        tvEnglish = findViewById(R.id.tv_detail_english);
        tvPhonetic = findViewById(R.id.tv_detail_phonetic);
        tvChinese = findViewById(R.id.tv_detail_chinese);
        tvExample = findViewById(R.id.tv_detail_example);
//        btnFavorite = findViewById(R.id.btn_detail_favorite);
        btnFamiliar = findViewById(R.id.btn_detail_familiar);
        btnBack = findViewById(R.id.btn_back);

        // 加载单词详情
        loadWordDetail();

        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());


        // 熟悉度按钮点击事件
        btnFamiliar.setOnClickListener(v -> {
            if (currentWord != null) {
                boolean newState = !currentWord.isFamiliar();
                currentWord.setFamiliar(newState);
                updateFamiliarUI();
                
                executorService.execute(() -> {
                    dbHelper.updateWordFamiliarity(currentWord.getId(), newState);
                });
            }
        });
    }

    private void loadWordDetail() {
        executorService.execute(() -> {
            currentWord = dbHelper.getWordById(wordId);
            
            runOnUiThread(() -> {
                if (currentWord != null) {
                    tvEnglish.setText(currentWord.getEnglish());
                    tvPhonetic.setText(currentWord.getPhonetic());
                    tvChinese.setText(currentWord.getChinese());
                    tvExample.setText(currentWord.getExample());
                    updateFamiliarUI();
                } else {
                    finish();
                }
            });
        });
    }


    private void updateFamiliarUI() {
        if (currentWord != null) {
            btnFamiliar.setImageResource(currentWord.isFamiliar() 
                    ? R.drawable.ic_familiar 
                    : R.drawable.ic_unfamiliar);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
        executorService.shutdown();
    }

    public static void start(AppCompatActivity activity, Word word) {
        Intent intent = new Intent(activity, WordDetailActivity.class);
        intent.putExtra("word_id", word.getId());
        activity.startActivity(intent);
    }
}
