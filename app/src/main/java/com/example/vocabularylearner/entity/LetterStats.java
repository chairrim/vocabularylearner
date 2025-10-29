package com.example.vocabularylearner.entity;

/**
 * 字母统计实体类：存储某个字母开头的单词总数和已熟悉数量
 */
public class LetterStats {
    private int totalCount;    // 该字母开头的单词总数
    private int familiarCount; // 该字母开头的已熟悉单词数

    // 无参构造
    public LetterStats() {}

    // 带参构造（直接初始化统计数据）
    public LetterStats(int totalCount, int familiarCount) {
        this.totalCount = totalCount;
        this.familiarCount = familiarCount;
    }

    // Getter 和 Setter（用于获取/修改统计数据）
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
}
