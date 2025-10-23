package com.colordiary;

public class MoodEntry {
    public Integer rating;
    public String emoji;
    public String notes;

    public MoodEntry(Integer rating, String emoji, String notes) {
        this.rating = rating;
        this.emoji = emoji;
        this.notes = notes;
    }
}

