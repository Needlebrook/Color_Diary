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
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("✅ Connected to MySQL!");
        } catch (Exception ex) {
            ex.printStackTrace();
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

        statsBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Statistics window coming soon!")
        );

        setVisible(true);
    }

    // possibly non-functional as of now.
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

    //keep this
    private Color getColorForMood(int mood) {
        switch (mood) {
            case 1: case 2: return Color.RED;
            case 3: case 4: return Color.ORANGE;
            case 5: case 6: return Color.YELLOW;
            case 7: case 8: return new Color(31, 198, 0); // light green
            case 9: case 10: return new Color(0, 128, 0); // dark green
            default: return Color.LIGHT_GRAY;
        }
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

    	for (int day = 1; day <= daysInMonth; day++) 
    	{ 
    		JButton dayBtn = new JButton(String.valueOf(day));
    		int finalDay = day; 
    		LocalDate thisDate = currentMonth.atDay(day);

    		dayBtn.addActionListener(e -> openDayDialog(finalDay));
    		
    		if (thisDate.equals(LocalDate.now())) {
        	    dayBtn.setBackground(Color.CYAN);   // highlight with cyan
        	    dayBtn.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        	    dayBtn.setFont(dayBtn.getFont().deriveFont(Font.BOLD));
        	}
    		calendarPanel.add(dayBtn); 
    	} 

    	monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
    	calendarPanel.revalidate(); calendarPanel.repaint(); 
    	}

    //day window
    private void openDayDialog(int day) {
        JDialog dialog = new JDialog(this, "Day Entry - " + day, true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(6, 2));

        JLabel ratingLabel = new JLabel("Mood Rating (1–10):");
        JTextField ratingField = new JTextField();
        JLabel emojiLabel = new JLabel("Emoji:");
        JTextField emojiField = new JTextField();
        JLabel textLabel = new JLabel("Notes:");
        JTextArea textArea = new JTextArea();

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

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

    // day window save data
    private void saveEntry(int day, String rating, String emoji, String notes) {
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO mood_entries (user_id, entry_date, mood_rating, emoji, text_entry) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE mood_rating = VALUES(mood_rating), emoji = VALUES(emoji), text_entry = VALUES(text_entry)"
        )) {
            LocalDate date = currentMonth.atDay(day);

            stmt.setInt(1, 1); // user_id
            stmt.setDate(2, java.sql.Date.valueOf(date));
            stmt.setInt(3, Integer.parseInt(rating));
            stmt.setString(4, emoji);
            stmt.setString(5, notes);

            stmt.executeUpdate();
            System.out.println("✅ Saved entry for " + date);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
