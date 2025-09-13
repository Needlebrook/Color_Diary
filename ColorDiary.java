import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import java.util.Map;


public class ColorDiary extends JFrame {
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private YearMonth currentMonth;
    private Connection conn;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/color_diary";
    private static final String DB_USER = "celia";
    private static final String DB_PASS = "tiger";

    private Map<LocalDate, JButton> dayButtonMap = new HashMap<>();

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
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        monthLabel = new JLabel("", JLabel.CENTER);
        JButton statsBtn = new JButton("Statistics");

        topPanel.add(prevBtn);
        topPanel.add(monthLabel);
        topPanel.add(nextBtn);
        topPanel.add(statsBtn);

        add(topPanel, BorderLayout.NORTH);

        calendarPanel = new JPanel(new GridLayout(0, 7));
        add(calendarPanel, BorderLayout.CENTER);

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
        switch (mood) {
            case 0: return Color.LIGHT_GRAY;
            case 1: case 2: return new Color(255, 51, 51); 
            case 3: case 4: return new Color(255, 128, 0); 
            case 5: case 6: return new Color(255, 255, 0); 
            case 7: case 8: return new Color(102, 255, 102); 
            case 9: case 10: return new Color(0, 204, 0);
            default: return Color.LIGHT_GRAY;
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
        dialog.setSize(600, 400);
        dialog.setLayout(new GridLayout(6, 2));

        JLabel ratingLabel = new JLabel("Mood Rating (1–10):");
        JTextField ratingField = new JTextField();
        JLabel emojiLabel = new JLabel("Emoji:");
        JTextField emojiField = new JTextField();
        JLabel textLabel = new JLabel("Notes:");
        JTextArea textArea = new JTextArea();

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        LocalDate date = currentMonth.atDay(day);
        MoodEntry entry = getEntryForDate(date);

    if (entry != null) {
        if (entry.rating != null) ratingField.setText(String.valueOf(entry.rating));
        if (entry.emoji != null) emojiField.setText(entry.emoji);
        if (entry.notes != null) textArea.setText(entry.notes);
    }

        dialog.add(ratingLabel);
        dialog.add(ratingField);
        dialog.add(emojiLabel);
        dialog.add(emojiField);
        dialog.add(textLabel);
        dialog.add(new JScrollPane(textArea));
        dialog.add(saveBtn);
        dialog.add(cancelBtn);

        saveBtn.addActionListener(e -> {
            saveEntry(day, ratingField.getText(), emojiField.getText(), textArea.getText());
            dialog.dispose();
            drawCalendar(); // ✅ refresh calendar after saving
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
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
        statsDialog.setSize(500, 400);
        statsDialog.setLayout(new BorderLayout());

        int totalEntries = 0;
        double avgMood = 0.0;
        int bestMood = Integer.MIN_VALUE;
        int worstMood = Integer.MAX_VALUE;

        // Store per-day stats
        Map<Integer, Integer> dayToMood = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT DAY(entry_date) as day, mood_rating FROM mood_entries WHERE user_id = 1"
        )) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int rating = rs.getInt("mood_rating");
                    int day = rs.getInt("day");
                    totalEntries++;
                    avgMood += rating;
                    bestMood = Math.max(bestMood, rating);
                    worstMood = Math.min(worstMood, rating);
                    dayToMood.put(day, rating); // last entry of that day
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (totalEntries > 0) avgMood /= totalEntries;
        if (bestMood == Integer.MIN_VALUE) bestMood = 0;
        if (worstMood == Integer.MAX_VALUE) worstMood = 0;

        // --- Stats summary ---
        JPanel summaryPanel = new JPanel(new GridLayout(0, 1));
        summaryPanel.add(new JLabel("Total Entries: " + totalEntries));
        summaryPanel.add(new JLabel("Average Mood: " + String.format("%.2f", avgMood)));
        summaryPanel.add(new JLabel("Best Mood: " + bestMood));
        summaryPanel.add(new JLabel("Worst Mood: " + worstMood));
        statsDialog.add(summaryPanel, BorderLayout.NORTH);

        // --- Custom chart panel (simple bar graph) ---
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int width = getWidth();
                int height = getHeight();

                int barWidth = Math.max(5, width / Math.max(1, dayToMood.size()));
                int maxMood = 10; // assuming mood rating scale 1–10

                int x = 10;
                for (Map.Entry<Integer, Integer> entry : dayToMood.entrySet()) {
                    int day = entry.getKey();
                    int mood = entry.getValue();
                    int barHeight = (int) ((double) mood / maxMood * (height - 50));
                    g.setColor(new Color(100, 150, 255));
                    g.fillRect(x, height - barHeight - 30, barWidth, barHeight);
                    g.setColor(Color.BLACK);
                    g.drawString(String.valueOf(day), x, height - 10);
                    x += barWidth + 5;
                }
            }
        };
        chartPanel.setPreferredSize(new Dimension(400, 250));
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
