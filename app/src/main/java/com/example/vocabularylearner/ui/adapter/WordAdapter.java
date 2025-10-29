package com.example.vocabularylearner.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularylearner.R;
import com.example.vocabularylearner.entity.Word;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
    private List<Word> wordList;
    private boolean isChineseVisible;
    private OnFamiliarityChangeListener familiarityChangeListener;
    private OnItemClickListener itemClickListener;

    // 构造方法
    public WordAdapter(List<Word> wordList, boolean isChineseVisible) {
        this.wordList = wordList;
        this.isChineseVisible = isChineseVisible;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        if (word == null) return;

        // 绑定单词数据
        holder.tvEnglish.setText(word.getEnglish());
        holder.tvPhonetic.setText(word.getPhonetic());
        holder.tvChinese.setText(word.getChinese());

        // 控制中文显示/隐藏：
        // 1. 如果设置为显示中文，则全部显示
        // 2. 如果设置为隐藏中文，则只隐藏未熟悉的单词的中文
        if (isChineseVisible) {
            holder.tvChinese.setVisibility(View.VISIBLE);
        } else {
            // 未熟悉的单词隐藏中文，已熟悉的单词显示中文
            holder.tvChinese.setVisibility(word.isFamiliar() ? View.VISIBLE : View.GONE);
        }

        // 设置熟悉度状态（使用加号和对勾图标）
        holder.btnFamiliar.setImageResource(word.isFamiliar()
                ? R.drawable.ic_familiar  // 对勾图标
                : R.drawable.ic_plus);    // 加号图标

        // 移除收藏按钮相关代码...

        // 熟悉度按钮点击事件
        holder.btnFamiliar.setOnClickListener(v -> {
            boolean newState = !word.isFamiliar();
            word.setFamiliar(newState);
            holder.btnFamiliar.setImageResource(newState
                    ? R.drawable.ic_familiar
                    : R.drawable.ic_plus);

            // 更新中文显示状态（因为熟悉度变化会影响中文是否显示）
            if (!isChineseVisible) {
                holder.tvChinese.setVisibility(newState ? View.VISIBLE : View.GONE);
            }

            if (familiarityChangeListener != null) {
                familiarityChangeListener.onFamiliarityChange(word, newState);
            }
        });

        // 单词项点击事件（跳转到详情页）
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(word);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList == null ? 0 : wordList.size();
    }

    // 更新中文显示状态
    public void setChineseVisible(boolean visible) {
        isChineseVisible = visible;
        notifyDataSetChanged(); // 刷新列表
    }

    //  ViewHolder类
    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView tvEnglish, tvPhonetic, tvChinese;
        ImageButton btnFavorite, btnFamiliar;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            // 绑定控件（与item_word.xml中的ID严格对应）
            tvEnglish = itemView.findViewById(R.id.tv_english);
            tvPhonetic = itemView.findViewById(R.id.tv_phonetic);
            tvChinese = itemView.findViewById(R.id.tv_chinese);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            btnFamiliar = itemView.findViewById(R.id.btn_familiar);
        }
    }

    // 熟悉度和收藏状态变化的监听器接口
    public interface OnFamiliarityChangeListener {
        void onFamiliarityChange(Word word, boolean isFamiliar);
        void onFavoriteChange(Word word, boolean isFavorite);
    }

    // 单词项点击监听器接口
    public interface OnItemClickListener {
        void onItemClick(Word word);
    }

    // 设置监听器的方法
    public void setFamiliarityChangeListener(OnFamiliarityChangeListener listener) {
        this.familiarityChangeListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }
}
