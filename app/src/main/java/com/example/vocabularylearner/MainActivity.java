package com.example.vocabularylearner;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularylearner.entity.LetterStats;
import com.example.vocabularylearner.db.WordDbHelper;
import com.example.vocabularylearner.entity.Letter;
import com.example.vocabularylearner.entity.Word;
import com.example.vocabularylearner.ui.WordListActivity;
import com.example.vocabularylearner.ui.adapter.LetterAdapter;
import com.example.vocabularylearner.utils.ExcelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private static final int READ_EXCEL_REQUEST_CODE = 1001;
    private static final int STORAGE_PERMISSION_REQUEST = 1002;

    private RecyclerView letterRecyclerView;
    private LetterAdapter letterAdapter;
    private WordDbHelper dbHelper;
    private ExecutorService executorService;
    private TextView tvPermissionHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);


        // 初始化
        dbHelper = new WordDbHelper(this);
        executorService = Executors.newSingleThreadExecutor();
        
        // 绑定控件
        letterRecyclerView = findViewById(R.id.recycler_letters);
        tvPermissionHint = findViewById(R.id.tv_permission_hint);


        // 1. 初始化头部Toolbar（绑定菜单）
        initToolbar();
        // 初始化字母列表
        initLetterRecyclerView();
        
        // 检查权限并加载字母数据
        checkStoragePermission();
    }


    // 初始化Toolbar：设置为ActionBar，绑定菜单
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        // 将Toolbar设置为当前Activity的ActionBar（适配Material3）
        setSupportActionBar(toolbar);
        // 可选：隐藏ActionBar默认标题（已在布局中设置app:title=""，此处可省略）
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    // 加载菜单（将head_menu.xml与Toolbar绑定）
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_menu, menu);
        return true; // 返回true表示显示菜单
    }

    private void initLetterRecyclerView() {
        List<Letter> letters = new ArrayList<>();
        // 添加A-Z字母
        for (char c = 'A'; c <= 'Z'; c++) {
            letters.add(new Letter(String.valueOf(c)));
        }
        
        letterAdapter = new LetterAdapter(letters);
        letterRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        letterRecyclerView.setAdapter(letterAdapter);
        
        // 字母卡片点击事件
        letterAdapter.setOnLetterClickListener(letter -> {
            Intent intent = new Intent(MainActivity.this, WordListActivity.class);
            intent.putExtra("letter", letter);
            startActivity(intent);
        });
    }

    private void loadLetterStats() {
        executorService.execute(() -> {
            for (int i = 0; i < letterAdapter.getItemCount(); i++) {
                Letter letter = letterAdapter.getItem(i);
                LetterStats stats = dbHelper.getLetterStats(letter.getLetter());
                
                int finalI = i;
                runOnUiThread(() -> {
                    letter.setTotalCount(stats.getTotalCount());
                    letter.setFamiliarCount(stats.getFamiliarCount());
                    letterAdapter.notifyItemChanged(finalI);
                });
            }
        });
    }

    private void checkStoragePermission() {
        // 检查存储权限（根据Android版本处理）
        if (ExcelUtils.hasStoragePermission(this)) {
            tvPermissionHint.setVisibility(View.GONE);
            loadLetterStats();
        } else {
            tvPermissionHint.setVisibility(View.VISIBLE);
            tvPermissionHint.setOnClickListener(v -> ExcelUtils.requestStoragePermission(this, STORAGE_PERMISSION_REQUEST));
        }
    }

    private void openFilePicker() {
        if (!ExcelUtils.hasStoragePermission(this)) {
            ExcelUtils.requestStoragePermission(this, STORAGE_PERMISSION_REQUEST);
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
        startActivityForResult(intent, READ_EXCEL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == READ_EXCEL_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                importExcelFile(uri);
            }
        }
    }

    private void importExcelFile(Uri uri) {
        showToast("开始导入单词...");
        
        executorService.execute(() -> {
            try {
                // 解析Excel文件
                List<Word> words = ExcelUtils.parseExcelFile(this, uri);
                
                if (words == null || words.isEmpty()) {
                    runOnUiThread(() -> {
                        showToast("未解析到单词数据，请检查文件格式");
                    });
                    return;
                }
                
                // 保存到数据库
                AtomicInteger newWordCount = new AtomicInteger(0);
                
                for (Word word : words) {
                    if (!dbHelper.isWordExists(word.getEnglish())) {
                        dbHelper.addWord(word);
                        newWordCount.incrementAndGet();
                    }
                }
                
                // 通知UI结果
                runOnUiThread(() -> {
                    int count = newWordCount.get();
                    if (count > 0) {
                        showToast("成功导入 " + count + " 个新单词");
                        loadLetterStats(); // 更新字母统计
                    } else {
                        showToast("所有单词已存在");
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showToast("导入失败: " + e.getMessage());
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            checkStoragePermission();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
        executorService.shutdown();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.btn_import2) {
            Toast.makeText(this, "执行导入操作", Toast.LENGTH_SHORT).show();
            // 此处添加导入逻辑（如打开文件选择器、读取数据等）
            openFilePicker();
            return true;
        } else if (itemId == R.id.action_export2) {
            Toast.makeText(this, "执行导出操作", Toast.LENGTH_SHORT).show();
            // 此处添加导出逻辑（如写入文件、保存数据等）
            return true;
        } else if (itemId == R.id.action_clear2) {
            showClearConfirmDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void showClearConfirmDialog() {
        new AlertDialog.Builder(this)  // Activity 中用 this 作为上下文
                .setTitle("确认清空")
                .setMessage("确定要清空所有单词数据吗？此操作不可恢复。")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 这里添加清空数据的逻辑
                    clearAllData(); // 例如调用清空方法
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void clearAllData() {
        executorService.execute(() -> {
            dbHelper.clearAllData();
            runOnUiThread(() -> {
                showToast("所有单词数据已清空");
                loadLetterStats(); // 刷新字母统计
            });
        });
    }
}
