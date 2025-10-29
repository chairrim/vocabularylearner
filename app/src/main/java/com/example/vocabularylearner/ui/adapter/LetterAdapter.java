package com.example.vocabularylearner.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularylearner.entity.Letter;
import com.example.vocabularylearner.R;

import java.util.List;

public class LetterAdapter extends RecyclerView.Adapter<LetterAdapter.LetterViewHolder> {
    private List<Letter> letterList;
    private OnLetterClickListener listener;

    public LetterAdapter(List<Letter> letterList) {
        this.letterList = letterList;
    }

    @NonNull
    @Override
    public LetterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_letter, parent, false);
        return new LetterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LetterViewHolder holder, int position) {
        Letter letter = letterList.get(position);
        holder.tvLetter.setText(letter.getLetter());
        
        // 显示统计信息
        String stats = letter.getFamiliarCount() + "/" + letter.getTotalCount();
        holder.tvStats.setText(stats);

        holder.tvProgress.setProgress(letter.getProgressPercent());
        
        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLetterClick(letter.getLetter());
            }
        });
    }

    @Override
    public int getItemCount() {
        return letterList.size();
    }

    public Letter getItem(int position) {
        return letterList.get(position);
    }

    public static class LetterViewHolder extends RecyclerView.ViewHolder {
        TextView tvLetter, tvStats;
        ProgressBar tvProgress;

        public LetterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLetter = itemView.findViewById(R.id.tv_letter);
            tvStats = itemView.findViewById(R.id.tv_stats);
            tvProgress = itemView.findViewById(R.id.tv_progress);
        }
    }

    public interface OnLetterClickListener {
        void onLetterClick(String letter);
    }

    public void setOnLetterClickListener(OnLetterClickListener listener) {
        this.listener = listener;
    }
}
