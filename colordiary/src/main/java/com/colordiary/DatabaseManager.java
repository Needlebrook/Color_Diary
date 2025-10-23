package com.colordiary;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:color_diary.db";
    private Connection conn;

    public DatabaseManager() {
        connect();
        createTableIfNotExist();
    }

    private void connect() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("✅ Connected to SQLite!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void createTableIfNotExist() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS mood_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    entry_date TEXT NOT NULL,
                    mood_rating INTEGER,
                    emoji TEXT,
                    text_entry TEXT,
                    UNIQUE(user_id, entry_date)
                )
            """);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- CRUD ---
    public Optional<MoodEntry> getEntry(int userId, LocalDate date) {
        String sql = "SELECT mood_rating, emoji, text_entry FROM mood_entries WHERE user_id = ? AND entry_date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, date.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new MoodEntry(
                        rs.getInt("mood_rating"),
                        rs.getString("emoji"),
                        rs.getString("text_entry")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    public void saveEntry(int userId, LocalDate date, int mood, String emoji, String notes) {
        if (mood <= 0) {
            deleteEntry(userId, date);
            return;
        }

        String sql = """
            INSERT INTO mood_entries (user_id, entry_date, mood_rating, emoji, text_entry)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(user_id, entry_date) DO UPDATE SET
                mood_rating = excluded.mood_rating,
                emoji = excluded.emoji,
                text_entry = excluded.text_entry
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, date.toString());
            stmt.setInt(3, mood);
            stmt.setString(4, emoji);
            stmt.setString(5, notes);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteEntry(int userId, LocalDate date) {
        String sql = "DELETE FROM mood_entries WHERE user_id = ? AND entry_date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, date.toString());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- Stats ---
    public ResultSet getStatsForMonth(int userId, YearMonth month) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT COUNT(*) as total, AVG(mood_rating) as avg " +
            "FROM mood_entries WHERE user_id = ? AND strftime('%m', entry_date)=? AND strftime('%Y', entry_date)=?"
        );
        stmt.setInt(1, userId);
        stmt.setString(2, String.format("%02d", month.getMonthValue()));
        stmt.setString(3, String.valueOf(month.getYear()));
        return stmt.executeQuery();
    }

    public ResultSet getMoodTrend(int userId, YearMonth month) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT CAST(strftime('%d', entry_date) AS INT) as day, mood_rating " +
            "FROM mood_entries WHERE user_id = ? AND strftime('%m', entry_date)=? AND strftime('%Y', entry_date)=? " +
            "ORDER BY entry_date"
        );
        stmt.setInt(1, userId);
        stmt.setString(2, String.format("%02d", month.getMonthValue()));
        stmt.setString(3, String.valueOf(month.getYear()));
        return stmt.executeQuery();
    }
}
