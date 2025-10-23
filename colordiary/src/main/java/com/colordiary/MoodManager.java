package com.colordiary;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

public class MoodManager {
    private DatabaseManager db;
    private String currentPalette = "Heatmap";
    private int userId = 1; // default user

    public MoodManager() {
        db = new DatabaseManager();
    }

    public void setPalette(String palette) { this.currentPalette = palette; }

    public void setUserId(int userId) { this.userId = userId; }

    public Color getColorForMood(int mood) {
        if (mood <= 0) return Color.LIGHT_GRAY;
        if (mood > 10) mood = 10;

        switch (currentPalette) {
            case "Heatmap":switch(mood){case 1: case 2: return new Color(255,51,51); case 3: case 4: return new Color(255,128,0); case 5: case 6: return new Color(255,255,0); case 7: case 8: return new Color(102,255,102); case 9: case 10: return new Color(0,204,0);}
            case "Ocean":switch(mood){case 1: case 2: return new Color(0,51,102); case 3: case 4: return new Color(0,102,204); case 5: case 6: return new Color(0,204,255); case 7: case 8: return new Color(102,255,204); case 9: case 10: return new Color(0,204,102);}
            case "Serenity":switch(mood){case 1: case 2: return new Color(25,25,112); case 3: case 4: return new Color(70,130,180); case 5: case 6: return new Color(135,206,235); case 7: case 8: return new Color(144,238,144); case 9: case 10: return new Color(0,200,0);}
            case "Sunscape":switch (mood){case 1: case 2: return new Color(178, 34, 34);case 3: case 4: return new Color(255, 69, 0);case 5: case 6: return new Color(255, 215, 0); case 7: case 8: return new Color(255, 255, 102);case 9: case 10: return new Color(255, 140, 0);}
            case "Mystic Fire":switch (mood) {case 1: case 2: return new Color(139, 0, 0);case 3: case 4: return new Color(178, 34, 34);case 5: case 6: return new Color(255, 69, 0);case 7: case 8: return new Color(255, 140, 0);case 9: case 10: return new Color(0, 0, 139);}
            case "Forestry":switch (mood) {case 1: case 2: return new Color(85, 107, 47);case 3: case 4: return new Color(107, 142, 35);case 5: case 6: return new Color(173, 255, 47); case 7: case 8: return new Color(124, 252, 0);case 9: case 10: return new Color(0, 200, 0);}
            case "Candy":switch (mood) {case 1: case 2: return new Color(255, 20, 147);case 3: case 4: return new Color(255, 182, 193);case 5: case 6: return new Color(221, 160, 221); case 7: case 8: return new Color(186, 85, 211);case 9: case 10: return new Color(148, 0, 211);}
            default: return Color.LIGHT_GRAY;
        }
    }

    // --- CRUD ---
    public MoodEntry getEntryForDate(LocalDate date) {
        Optional<MoodEntry> opt = db.getEntry(userId, date);
        return opt.orElse(null);
    }

    public void saveEntry(LocalDate date, int mood, String emoji, String notes) {
        db.saveEntry(userId, date, mood, emoji, notes);
    }

    public void deleteEntry(LocalDate date) {
        db.deleteEntry(userId, date);
    }

    // --- Stats ---
    public ResultSet getStatsForMonth(YearMonth month) throws SQLException {
        return db.getStatsForMonth(userId, month);
    }

    public ResultSet getMoodTrend(YearMonth month) throws SQLException {
        return db.getMoodTrend(userId, month);
    }
}
