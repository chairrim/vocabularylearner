package com.example.vocabularylearner.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.vocabularylearner.entity.LetterStats;
import com.example.vocabularylearner.entity.Word;

import java.util.ArrayList;
import java.util.List;

public class WordDbHelper extends SQLiteOpenHelper {
    // 数据库信息
    private static final String DATABASE_NAME = "Vocabulary.db";
    private static final int DATABASE_VERSION = 1;
    
    // 单词表
    private static final String TABLE_WORDS = "words";
    
    // 表字段
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_ENGLISH = "english";
    private static final String COLUMN_PHONETIC = "phonetic";
    private static final String COLUMN_CHINESE = "chinese";
    private static final String COLUMN_EXAMPLE = "example";
    private static final String COLUMN_FAVORITE = "is_favorite";
    private static final String COLUMN_FAMILIAR = "is_familiar";
    private static final String COLUMN_FIRST_LETTER = "first_letter";

    // 创建表SQL
    private static final String CREATE_TABLE_WORDS = "CREATE TABLE " + TABLE_WORDS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ENGLISH + " TEXT NOT NULL,"
            + COLUMN_PHONETIC + " TEXT,"
            + COLUMN_CHINESE + " TEXT,"
            + COLUMN_EXAMPLE + " TEXT,"
            + COLUMN_FAVORITE + " INTEGER DEFAULT 0,"
            + COLUMN_FAMILIAR + " INTEGER DEFAULT 0,"
            + COLUMN_FIRST_LETTER + " TEXT NOT NULL)";

    public WordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建单词表
        db.execSQL(CREATE_TABLE_WORDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级数据库时删除旧表并创建新表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        onCreate(db);
    }

    /**
     * 添加单词
     */
    public long addWord(Word word) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // 计算首字母（转为大写）
        String firstLetter = "";
        if (word.getEnglish() != null && !word.getEnglish().isEmpty()) {
            firstLetter = word.getEnglish().substring(0, 1).toUpperCase();
        }
        
        values.put(COLUMN_ENGLISH, word.getEnglish());
        values.put(COLUMN_PHONETIC, word.getPhonetic());
        values.put(COLUMN_CHINESE, word.getChinese());
        values.put(COLUMN_EXAMPLE, word.getExample());
        values.put(COLUMN_FAVORITE, word.isFavorite() ? 1 : 0);
        values.put(COLUMN_FAMILIAR, word.isFamiliar() ? 1 : 0);
        values.put(COLUMN_FIRST_LETTER, firstLetter);

        // 插入数据并获取ID
        long id = db.insert(TABLE_WORDS, null, values);
        db.close();
        return id;
    }

    /**
     * 检查单词是否已存在
     */
    public boolean isWordExists(String english) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_WORDS,
                new String[]{COLUMN_ID},
                COLUMN_ENGLISH + " = ?",
                new String[]{english},
                null, null, null
        );
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * 根据首字母获取单词列表
     */
    public List<Word> getWordsByLetter(String letter) {
        List<Word> words = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
                TABLE_WORDS,
                null,
                COLUMN_FIRST_LETTER + " = ?",
                new String[]{letter.toUpperCase()},
                null, null,
                COLUMN_ENGLISH + " ASC" // 按字母顺序排序
        );

        if (cursor.moveToFirst()) {
            do {
                Word word = new Word();
                word.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                word.setEnglish(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH)));
                word.setPhonetic(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONETIC)));
                word.setChinese(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHINESE)));
                word.setExample(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXAMPLE)));
                word.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1);
                word.setFamiliar(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAMILIAR)) == 1);
                
                words.add(word);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return words;
    }

    /**
     * 更新单词熟悉度
     */
    public int updateWordFamiliarity(long wordId, boolean isFamiliar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAMILIAR, isFamiliar ? 1 : 0);
        
        int rowsAffected = db.update(
                TABLE_WORDS,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(wordId)}
        );
        
        db.close();
        return rowsAffected;
    }

    /**
     * 更新单词收藏状态
     */
    public int updateWordFavorite(long wordId, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORITE, isFavorite ? 1 : 0);
        
        int rowsAffected = db.update(
                TABLE_WORDS,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(wordId)}
        );
        
        db.close();
        return rowsAffected;
    }

    /**
     * 获取字母统计信息（用于主页字母卡片）
     */
    public LetterStats getLetterStats(String letter) {
        LetterStats stats = new LetterStats();
        SQLiteDatabase db = this.getReadableDatabase();
        String targetLetter = letter.toUpperCase();
        
        // 查询总数
        String totalSql = "SELECT COUNT(*) AS total FROM " + TABLE_WORDS + 
                         " WHERE " + COLUMN_FIRST_LETTER + " = ?";
        Cursor totalCursor = db.rawQuery(totalSql, new String[]{targetLetter});
        if (totalCursor.moveToFirst()) {
            stats.setTotalCount(totalCursor.getInt(totalCursor.getColumnIndexOrThrow("total")));
        }
        totalCursor.close();
        
        // 查询已熟悉数量
        String familiarSql = "SELECT COUNT(*) AS familiar FROM " + TABLE_WORDS + 
                            " WHERE " + COLUMN_FIRST_LETTER + " = ?" + 
                            " AND " + COLUMN_FAMILIAR + " = 1";
        Cursor familiarCursor = db.rawQuery(familiarSql, new String[]{targetLetter});
        if (familiarCursor.moveToFirst()) {
            stats.setFamiliarCount(familiarCursor.getInt(familiarCursor.getColumnIndexOrThrow("familiar")));
        }
        familiarCursor.close();
        db.close();
        
        return stats;
    }

    /**
     * 根据ID获取单词详情
     */
    public Word getWordById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Word word = null;
        
        Cursor cursor = db.query(
                TABLE_WORDS,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            word = new Word();
            word.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            word.setEnglish(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH)));
            word.setPhonetic(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONETIC)));
            word.setChinese(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHINESE)));
            word.setExample(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXAMPLE)));
            word.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1);
            word.setFamiliar(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAMILIAR)) == 1);
        }
        
        cursor.close();
        db.close();
        return word;
    }

    //清空所有单词
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORDS, null, null);
        db.close();
    }


    /**
     * 根据首字母和熟悉度获取单词列表
     */
    public List<Word> getWordsByLetterAndFamiliarity(String letter, boolean isFamiliar) {
        List<Word> words = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_WORDS,
                null,
                COLUMN_FIRST_LETTER + " = ? AND " + COLUMN_FAMILIAR + " = ?",
                new String[]{letter.toUpperCase(), isFamiliar ? "1" : "0"},
                null, null,
                COLUMN_ENGLISH + " ASC" // 按字母顺序排序
        );

        if (cursor.moveToFirst()) {
            do {
                Word word = new Word();
                word.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                word.setEnglish(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH)));
                word.setPhonetic(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONETIC)));
                word.setChinese(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHINESE)));
                word.setExample(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXAMPLE)));
                word.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1);
                word.setFamiliar(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAMILIAR)) == 1);

                words.add(word);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return words;
    }

    /**
 * 获取所有单词
 */
public List<Word> getAllWords() {
    List<Word> words = new ArrayList<>();
    SQLiteDatabase db = this.getReadableDatabase();

    Cursor cursor = db.query(
            TABLE_WORDS,
            null, // 所有列
            null, // 没有 WHERE 子句
            null, // 没有 WHERE 参数
            null, // 没有 GROUP BY
            null, // 没有 HAVING
            COLUMN_ENGLISH + " ASC" // 按英文单词排序
    );

    if (cursor.moveToFirst()) {
        do {
            Word word = new Word();
            word.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            word.setEnglish(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH)));
            word.setPhonetic(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONETIC)));
            word.setChinese(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHINESE)));
            word.setExample(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXAMPLE)));
            word.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1);
            word.setFamiliar(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAMILIAR)) == 1);
            words.add(word);
        } while (cursor.moveToNext());
    }

    cursor.close();
    db.close();
    return words;
}



}
