package com.colordiary;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;


public class ColorDiary extends JFrame {
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private YearMonth currentMonth;
    private Connection conn;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/color_diary";
    private static final String DB_USER = "celia";
    private static final String DB_PASS = "tiger";

    private Map<LocalDate, JButton> dayButtonMap = new HashMap<>();
    private String currentPalette = "Heatmap"; 


    //MySQL connection
    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, "tiger");
            System.out.println("✅ Connected to MySQL!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class MoodEntry {
        Integer rating;
        String emoji;
        String notes;

        MoodEntry(Integer rating, String emoji, String notes) {
            this.rating = rating;
            this.emoji = emoji;
            this.notes = notes;
        }
    }

    // main calender frame
    public ColorDiary() {
        connectDatabase();

        setTitle("Color Diary");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        monthLabel = new JLabel("", JLabel.CENTER);
        JButton statsBtn = new JButton("Statistics");
        String[] palettes = {
            "Heatmap",   
            "Ocean",   
            "Sunscape",
            "Serenity",
            "Mystic Fire",    
            "Forestry",    
            "Candy"   
        };

        JComboBox<String> paletteSelector = new JComboBox<>(palettes);
        paletteSelector.addActionListener(e -> {
            currentPalette = (String) paletteSelector.getSelectedItem();
            drawCalendar(); // refresh calendar
        });

        topPanel.add(paletteSelector);


        topPanel.add(prevBtn);
        topPanel.add(monthLabel);
        topPanel.add(nextBtn);
        topPanel.add(statsBtn);

        add(topPanel, BorderLayout.NORTH);

        calendarPanel = new JPanel(new GridLayout(0, 7));
        add(calendarPanel, BorderLayout.CENTER);
        calendarPanel.setPreferredSize(new Dimension(1000, 750));
        currentMonth = YearMonth.now();
        drawCalendar();

        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            drawCalendar();
        });

        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            drawCalendar();
        });

        statsBtn.addActionListener(e -> openStatsWindow());


        setVisible(true);
    }

    private Integer getMoodForDate(LocalDate date) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT mood_rating FROM mood_entries WHERE user_id = 1 AND entry_date = ?")) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("mood_rating");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Color getColorForMood(int mood) {
        if (mood <= 0) return Color.LIGHT_GRAY;
        if (mood > 10) mood = 10;

        switch (currentPalette) {
            case "Heatmap": 
                switch (mood) {
                    case 1: case 2: return new Color(255, 51, 51);   // bright red
                    case 3: case 4: return new Color(255, 128, 0);   // vivid orange
                    case 5: case 6: return new Color(255, 255, 0);   // yellow
                    case 7: case 8: return new Color(102, 255, 102); // lime green
                    case 9: case 10: return new Color(0, 204, 0);    // deep green
                }

            case "Ocean":
                switch (mood) {
                    case 1: case 2: return new Color(0, 51, 102);    // 🌊 deep navy
                    case 3: case 4: return new Color(0, 102, 204);   // 💧 blue
                    case 5: case 6: return new Color(0, 204, 255);   // 🌐 cyan
                    case 7: case 8: return new Color(102, 255, 204); // 🟢 aqua green
                    case 9: case 10: return new Color(0, 204, 102);  // 🪸 teal green
                }

            case "Serenity":
                switch (mood) {
                    case 1: case 2: return new Color(25, 25, 112);   // 🌑 midnight blue (sad/low)
                    case 3: case 4: return new Color(70, 130, 180);  // 🌊 steel blue (calm)
                    case 5: case 6: return new Color(135, 206, 235); // ☁️ sky blue (neutral)
                    case 7: case 8: return new Color(144, 238, 144); // 🌱 light green (hopeful)
                    case 9: case 10: return new Color(0, 200, 0);    // 🌳 vibrant green (happy)
                }

            case "Sunscape":
                switch (mood) {
                    case 1: case 2: return new Color(178, 34, 34);   // 🔴 fire red
                    case 3: case 4: return new Color(255, 69, 0);   // 🟠 orange red
                    case 5: case 6: return new Color(255, 215, 0);  // 🟡 gold
                    case 7: case 8: return new Color(255, 255, 102);// 🌞 light yellow
                    case 9: case 10: return new Color(255, 140, 0); // 🌇 dark orange
                }

            case "Mystic Fire":
                switch (mood) {
                    case 1: case 2: return new Color(139, 0, 0);    // 🔴 dark red
                    case 3: case 4: return new Color(178, 34, 34);  // 🟥 crimson
                    case 5: case 6: return new Color(255, 69, 0);   // 🟠 flame
                    case 7: case 8: return new Color(255, 140, 0);  // 🔶 dark orange
                    case 9: case 10: return new Color(0, 0, 139);   // 🔵 deep blue
                }


            case "Forestry":
                switch (mood) {
                    case 1: case 2: return new Color(85, 107, 47);   // 🌲 dark olive green (dull/sad)
                    case 3: case 4: return new Color(107, 142, 35);  // 🍃 olive drab (low)
                    case 5: case 6: return new Color(173, 255, 47);  // 🌼 green-yellow (neutral fresh)
                    case 7: case 8: return new Color(124, 252, 0);   // 🍏 lawn green (hopeful)
                    case 9: case 10: return new Color(0, 200, 0);    // 🌳 vibrant green (happy/strong)
                }

            case "Candy":
            switch (mood) {
                case 1: case 2: return new Color(255, 20, 147);  // deep pink
                case 3: case 4: return new Color(255, 182, 193); // light pink
                case 5: case 6: return new Color(221, 160, 221); // plum
                case 7: case 8: return new Color(186, 85, 211);  // orchid
                case 9: case 10: return new Color(148, 0, 211);  // dark violet
            }

            default:
                return Color.LIGHT_GRAY;
        }
    }

    private MoodEntry getEntryForDate(LocalDate date) {
    try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT mood_rating, emoji, text_entry FROM mood_entries " +
            "WHERE user_id = 1 AND entry_date = ?")) {
        stmt.setDate(1, java.sql.Date.valueOf(date));
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new MoodEntry(
                    rs.getInt("mood_rating"),
                    rs.getString("emoji"),
                    rs.getString("text_entry")
                );
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

    //fills in days
    private void drawCalendar() { 
    	calendarPanel.removeAll();
    	
    	String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    	for (String d : days) {
    	    JLabel lbl = new JLabel(d, SwingConstants.CENTER);
    	    lbl.setFont(new Font("Arial", Font.BOLD, 14));
    	    calendarPanel.add(lbl);
    	}

    	LocalDate firstDay = currentMonth.atDay(1); 
    	int daysInMonth = currentMonth.lengthOfMonth(); 
    	int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; 
    	// 1=Mon, 7=Sun // Fill blank cells before start of month 
    	for (int i = 0; i < startDayOfWeek; i++) {
    	    calendarPanel.add(new JLabel(""));
    	}

    	for (int day = 1; day <= daysInMonth; day++) { 
            JButton dayBtn = new JButton(String.valueOf(day));
            int finalDay = day; 
            LocalDate thisDate = currentMonth.atDay(day);

            // mood from DB
            MoodEntry entry = getEntryForDate(thisDate);
            if (entry != null && entry.rating != null) {
                dayBtn.setBackground(getColorForMood(entry.rating));
            } else {
            dayBtn.setBackground(Color.LIGHT_GRAY);
        }

            // Highlight today
            if (thisDate.equals(LocalDate.now())) {
                dayBtn.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                dayBtn.setFont(dayBtn.getFont().deriveFont(Font.BOLD));
            }

            dayBtn.addActionListener(e -> openDayDialog(finalDay));
            calendarPanel.add(dayBtn); 
        }

        monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
        calendarPanel.revalidate(); calendarPanel.repaint(); 
    }

    //day window
    private void openDayDialog(int day) {
        JDialog dialog = new JDialog(this, "Day Entry - " + day, true);
        dialog.pack();
        dialog.setSize(750, 500);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JLabel ratingLabel = new JLabel("Mood Rating (1–10):");
        JTextField ratingField = new JTextField();
        JLabel emojiLabel = new JLabel("Emoji:");
        JTextField emojiField = new JTextField();
        JButton emojiBtn = new JButton("😀");

        emojiBtn.addActionListener(e -> {
            JPopupMenu emojiMenu = new JPopupMenu();
            String[] emojis = {"😀","😢","😡","😴","❤️","🔥","🌊","🌿","🌟"};
            for (String em : emojis) {
                JMenuItem item = new JMenuItem(em);
                item.addActionListener(ev -> emojiField.setText(em));
                emojiMenu.add(item);
            }
            emojiMenu.show(emojiBtn, 0, emojiBtn.getHeight());
        });

        //buttons together
        JPanel emojiPanel = new JPanel(new BorderLayout());
        emojiPanel.add(emojiField, BorderLayout.CENTER);
        emojiPanel.add(emojiBtn, BorderLayout.EAST);

        // Add to top panel
        topPanel.add(ratingLabel);
        topPanel.add(ratingField);
        topPanel.add(emojiLabel);
        topPanel.add(emojiPanel);

        //notes field
        JLabel textLabel = new JLabel("Notes:");
        JTextArea textArea = new JTextArea(10, 40); // larger text area
        textArea.setLineWrap(true);                 // wrap long lines
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(textLabel, BorderLayout.WEST);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        LocalDate date = currentMonth.atDay(day);
        MoodEntry entry = getEntryForDate(date);
        if (entry != null) {
            if (entry.rating != null) ratingField.setText(String.valueOf(entry.rating));
            if (entry.emoji != null) emojiField.setText(entry.emoji);
            if (entry.notes != null) textArea.setText(entry.notes);
        }

        saveBtn.addActionListener(e -> {
            saveEntry(day, ratingField.getText(), emojiField.getText(), textArea.getText());
            dialog.dispose();
            drawCalendar();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }



    
    private void saveEntry(int day, String rating, String emoji, String notes) {
        try {
            LocalDate date = currentMonth.atDay(day);
            int mood = Integer.parseInt(rating);

            if (mood == 0) {
                try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM mood_entries WHERE user_id = ? AND entry_date = ?"
                )) {
                    stmt.setInt(1, 1); // user_id
                    stmt.setDate(2, java.sql.Date.valueOf(date));
                    stmt.executeUpdate();
                }
                System.out.println("🟦 Entry deleted for " + date);
            }else {
                // Normal save
                try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO mood_entries (user_id, entry_date, mood_rating, emoji, text_entry) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE mood_rating = VALUES(mood_rating), emoji = VALUES(emoji), text_entry = VALUES(text_entry)"
                )) {
                    stmt.setInt(1, 1); // user_id
                    stmt.setDate(2, java.sql.Date.valueOf(date));
                    stmt.setInt(3, mood);
                    stmt.setString(4, emoji);
                    stmt.setString(5, notes);
                    stmt.executeUpdate();
                }
                System.out.println("✅ Saved entry for " + date);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openStatsWindow() {
        JDialog statsDialog = new JDialog(this, "Statistics", true);
        statsDialog.setSize(800, 500);
        statsDialog.setLayout(new BorderLayout());

        // --- Step 1: Get stats from DB ---
        int totalEntries = 0;
        double avgMood = 0.0;

        try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT COUNT(*) as total, AVG(mood_rating) as avg FROM mood_entries WHERE user_id = 1 AND MONTH(entry_date) = ? AND YEAR(entry_date) = ?"
        )) {
            stmt.setInt(1, currentMonth.getMonthValue());
            stmt.setInt(2, currentMonth.getYear());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalEntries = rs.getInt("total");
                    avgMood = rs.getDouble("avg");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // --- Step 2: Summary labels ---
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1));
        summaryPanel.add(new JLabel("Entries this month: " + totalEntries));
        summaryPanel.add(new JLabel("Average mood: " + String.format("%.2f", avgMood)));

        statsDialog.add(summaryPanel, BorderLayout.NORTH);

        // --- Step 3: Mood trend chart (bar chart) ---
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT DAY(entry_date) as day, mood_rating FROM mood_entries WHERE user_id = 1 AND MONTH(entry_date) = ? AND YEAR(entry_date) = ? ORDER BY entry_date"
        )) {
            stmt.setInt(1, currentMonth.getMonthValue());
            stmt.setInt(2, currentMonth.getYear());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int day = rs.getInt("day");
                    int rating = rs.getInt("mood_rating");
                    dataset.addValue(rating, "Mood", String.valueOf(day));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Mood Trend - " + currentMonth.getMonth(),
            "Day",
            "Mood Rating",
            dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 400));
        statsDialog.add(chartPanel, BorderLayout.CENTER);


        statsDialog.setLocationRelativeTo(this);
        statsDialog.setVisible(true);
    }


    public static void main(String[] args) {
    	//splash/intro screen 
    	JWindow splash = new JWindow();
    	JLabel label = new JLabel("Color Diary", SwingConstants.CENTER); 
    	label.setFont(new Font("Arial", Font.BOLD, 36)); 
    	label.setForeground(Color.WHITE); 
    	
    	JPanel panel = new JPanel(new BorderLayout()); 
    	panel.setBackground(new Color(44, 150, 53)); 
    	panel.add(label, BorderLayout.CENTER); 
    	
    	splash.add(panel); 
    	splash.setSize(400, 200); 
    	splash.setLocationRelativeTo(null); 
    	splash.setVisible(true); 
    	
    	new Timer().schedule(new TimerTask() {
    		@Override public void run() { 
    			splash.setVisible(false); 
    			splash.dispose(); 
    			SwingUtilities.invokeLater(() -> new LoginScreen()); } }, 3000); 
    }
}
