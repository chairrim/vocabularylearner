package com.example.vocabularylearner.entity;

public class Letter {
    private String letter;
    private int totalCount;
    private int familiarCount;

    public Letter(String letter) {
        this.letter = letter;
        this.totalCount = 0;
        this.familiarCount = 0;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getFamiliarCount() {
        return familiarCount;
    }

    public void setFamiliarCount(int familiarCount) {
        this.familiarCount = familiarCount;
    }

    // 计算进度百分比（0-100）
    public int getProgressPercent() {
        if (totalCount == 0) return 0;
        return (int) ((float) familiarCount / totalCount * 100);
    }
}
