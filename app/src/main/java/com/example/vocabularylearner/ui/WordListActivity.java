package com.example.vocabularylearner.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularylearner.R;
import com.example.vocabularylearner.entity.Word;
import com.example.vocabularylearner.db.WordDbHelper;
import com.example.vocabularylearner.ui.adapter.WordAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WordAdapter wordAdapter;
    private WordDbHelper dbHelper;
    private String currentLetter;
    private boolean isChineseVisible = true;
    private Button btnToggleChinese;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);

        // 初始化数据库和线程池
        dbHelper = new WordDbHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        // 获取传递的字母参数
        currentLetter = getIntent().getStringExtra("letter");
        if (currentLetter == null) {
            finish();
            return;
        }

        // 初始化工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentLetter + " 开头的单词");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 初始化标题
        TextView tvLetterTitle = findViewById(R.id.tv_letter_title);
        tvLetterTitle.setText(currentLetter + " 开头的单词");

        // 初始化切换中文按钮
        btnToggleChinese = findViewById(R.id.btn_toggle_chinese);
        updateToggleButtonState();
        
        // 设置按钮点击事件
        btnToggleChinese.setOnClickListener(v -> {
            isChineseVisible = !isChineseVisible;
            updateToggleButtonState();
            if (wordAdapter != null) {
                wordAdapter.setChineseVisible(isChineseVisible);
            }

        });

        // 初始化RecyclerView
        recyclerView = findViewById(R.id.recycler_view_words);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // 加载单词数据
        loadWordsByLetter(currentLetter);
    }

    /**
     * 更新切换按钮的图标和文本
     */
    private void updateToggleButtonState() {
        if (isChineseVisible) {
            btnToggleChinese.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_hide_chinese, 0, 0, 0);
            btnToggleChinese.setText("隐藏中文");
        } else {
            btnToggleChinese.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_show_chinese, 0, 0, 0);
            btnToggleChinese.setText("显示中文");
        }
    }

    /**
     * 根据字母加载单词
     */
    private void loadWordsByLetter(String letter) {
        executorService.execute(() -> {
            List<Word> words = dbHelper.getWordsByLetter(letter);
            
            runOnUiThread(() -> {
                if (words.isEmpty()) {
                    Toast.makeText(this, "没有找到以 " + letter + " 开头的单词", 
                            Toast.LENGTH_SHORT).show();
                }
                
                // 初始化适配器
                wordAdapter = new WordAdapter(words, isChineseVisible);
                recyclerView.setAdapter(wordAdapter);
                
                // 设置熟悉度变化监听器
                wordAdapter.setFamiliarityChangeListener(new WordAdapter.OnFamiliarityChangeListener() {
                    @Override
                    public void onFamiliarityChange(Word word, boolean isFamiliar) {
                        executorService.execute(() -> {
                            dbHelper.updateWordFamiliarity(word.getId(), isFamiliar);
                        });
                    }

                    @Override
                    public void onFavoriteChange(Word word, boolean isFavorite) {
                        executorService.execute(() -> {
                            dbHelper.updateWordFavorite(word.getId(), isFavorite);
                        });
                    }
                });
                
                // 设置单词点击监听器（跳转到详情页）
                wordAdapter.setOnItemClickListener(word -> {
                    Intent intent = new Intent(WordListActivity.this, WordDetailActivity.class);
                    intent.putExtra("word_id", word.getId());
                    startActivity(intent);
                });
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // 处理返回按钮
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭数据库和线程池
        dbHelper.close();
        executorService.shutdown();
    }
}
