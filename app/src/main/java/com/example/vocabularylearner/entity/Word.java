package com.example.vocabularylearner.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Word implements Parcelable {
    private long id;
    private String english;
    private String phonetic;
    private String chinese;
    private String example;
    private boolean isFavorite;
    private boolean isFamiliar;

    public Word() {}

    public Word(String english, String phonetic, String chinese, String example) {
        this.english = english;
        this.phonetic = phonetic;
        this.chinese = chinese;
        this.example = example;
        this.isFavorite = false;
        this.isFamiliar = false;
    }

    protected Word(Parcel in) {
        id = in.readLong();
        english = in.readString();
        phonetic = in.readString();
        chinese = in.readString();
        example = in.readString();
        isFavorite = in.readByte() != 0;
        isFamiliar = in.readByte() != 0;
    }

    public static final Creator<Word> CREATOR = new Creator<Word>() {
        @Override
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public String getChinese() {
        return chinese;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isFamiliar() {
        return isFamiliar;
    }

    public void setFamiliar(boolean familiar) {
        isFamiliar = familiar;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(english);
        dest.writeString(phonetic);
        dest.writeString(chinese);
        dest.writeString(example);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeByte((byte) (isFamiliar ? 1 : 0));
    }
}
