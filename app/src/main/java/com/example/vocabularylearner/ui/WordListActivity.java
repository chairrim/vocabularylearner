package com.example.vocabularylearner.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.vocabularylearner.R;
import com.example.vocabularylearner.ui.adapter.WordPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class WordListActivity extends AppCompatActivity {
    private String currentLetter;
    private boolean isChineseVisible = true;
//    private Button btnToggleChinese;
    private WordListFragment[] fragments = new WordListFragment[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);

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
//
//        // 初始化标题
//        TextView tvLetterTitle = findViewById(R.id.tv_letter_title);
//        tvLetterTitle.setText(currentLetter + " 开头的单词");
//
//        // 初始化切换中文按钮
//        btnToggleChinese = findViewById(R.id.btn_toggle_chinese);
//        updateToggleButtonState();

        // 设置按钮点击事件
        // 替换 btnToggleChinese 的点击事件处理
//        btnToggleChinese.setOnClickListener(v -> {
//            isChineseVisible = !isChineseVisible;
//            updateToggleButtonState();
//
//            // 通过 FragmentManager 获取当前显示的 fragments 并更新状态
//            getSupportFragmentManager().getFragments().forEach(fragment -> {
//                if (fragment instanceof WordListFragment) {
//                    ((WordListFragment) fragment).setChineseVisible(isChineseVisible);
//                }
//            });
//        });

        // 初始化ViewPager2和TabLayout
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        // 创建适配器
        WordPagerAdapter pagerAdapter = new WordPagerAdapter(this, currentLetter);
        viewPager.setAdapter(pagerAdapter);

        // 缓存两个页面
        viewPager.setOffscreenPageLimit(2);


        // 关联TabLayout和ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(position == 0 ? "未熟悉" : "已熟悉");
                }).attach();

        // 初始化fragments引用
        fragments[0] = WordListFragment.newInstance(currentLetter, false);
        fragments[1] = WordListFragment.newInstance(currentLetter, true);
    }

    // 添加菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.word_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_chinese) {
            isChineseVisible = !isChineseVisible;

            // 通过 FragmentManager 获取当前显示的 fragments 并更新状态
            getSupportFragmentManager().getFragments().forEach(fragment -> {
                if (fragment instanceof WordListFragment) {
                    ((WordListFragment) fragment).setChineseVisible(isChineseVisible);
                }
            });

            // 更新菜单项文本
            if (isChineseVisible) {
                item.setTitle("隐藏中文");
            } else {
                item.setTitle("显示中文");
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * 更新切换按钮的图标和文本
     */
//    private void updateToggleButtonState() {
//        if (isChineseVisible) {
//            // 显示状态，按钮文本改为"隐藏中文"
//            btnToggleChinese.setText("隐藏中文");
//        } else {
//            // 隐藏状态，按钮文本改为"显示中文"
//            btnToggleChinese.setText("显示中文");
//        }
//    }
    @Override
    public boolean onSupportNavigateUp() {
        // 处理返回按钮
        finish();
        return true;
    }
}