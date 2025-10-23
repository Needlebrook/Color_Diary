package com.colordiary;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class ColorDiary extends JFrame {
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private YearMonth currentMonth;

    private MoodManager moodManager;
    private StatsManager statsManager;

    private Map<LocalDate, JButton> dayButtonMap = new HashMap<>();

    public ColorDiary() {
        this.moodManager = new MoodManager();
        this.statsManager = new StatsManager(moodManager);
        JButton weatherBtn = new JButton("Weather");

        setTitle("Color Diary");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarPanel.setPreferredSize(new Dimension(900, 650));
        add(calendarPanel, BorderLayout.CENTER);

        currentMonth = YearMonth.now();

        JPanel topPanel = new JPanel();
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        monthLabel = new JLabel("", JLabel.CENTER);
        JButton statsBtn = new JButton("Statistics");

        String[] palettes = {"Heatmap", "Ocean", "Sunscape", "Serenity", "Mystic Fire", "Forestry", "Candy"};
        JComboBox<String> paletteSelector = new JComboBox<>(palettes);
        paletteSelector.addActionListener(e -> {
            moodManager.setPalette((String) paletteSelector.getSelectedItem());
            drawCalendar();
        });
        paletteSelector.setSelectedItem("Heatmap");
        moodManager.setPalette("Heatmap");

        topPanel.add(weatherBtn);
        topPanel.add(paletteSelector);
        topPanel.add(prevBtn);
        topPanel.add(monthLabel);
        topPanel.add(nextBtn);
        topPanel.add(statsBtn);
        add(topPanel, BorderLayout.NORTH);

        // Button listeners 
        weatherBtn.addActionListener(e -> new WeatherWindow());

        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            drawCalendar();
        });

        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            drawCalendar();
        });

        statsBtn.addActionListener(e -> statsManager.showStatsWindow(this, currentMonth));

        // --- Draw initial calendar ---
        drawCalendar();

        setVisible(true);
    }


    // --- Calendar rendering ---
     private void drawCalendar() { 
        calendarPanel.removeAll();
        calendarPanel.setLayout(new GridLayout(0, 7, 0, 0)); // no gaps

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 14));
            calendarPanel.add(lbl);
        }

        LocalDate firstDay = currentMonth.atDay(1); 
        int daysInMonth = currentMonth.lengthOfMonth(); 
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; 

        for (int i = 0; i < startDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= daysInMonth; day++) { 
            JButton dayBtn = new JButton(String.valueOf(day));
            int finalDay = day; 
            LocalDate thisDate = currentMonth.atDay(day);

            MoodEntry entry = moodManager.getEntryForDate(thisDate);
            if (entry != null && entry.rating != null) {
                dayBtn.setBackground(moodManager.getColorForMood(entry.rating));
            } else {
                dayBtn.setBackground(Color.LIGHT_GRAY);
            }

            if (thisDate.equals(LocalDate.now())) {
                dayBtn.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                dayBtn.setFont(dayBtn.getFont().deriveFont(Font.BOLD));
            }

            dayBtn.addActionListener(e -> openDayDialog(finalDay));
            calendarPanel.add(dayBtn); 
            dayButtonMap.put(thisDate, dayBtn);
        }

        monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
        calendarPanel.revalidate(); 
        calendarPanel.repaint(); 
    }


    // --- Day entry dialog ---
    private void openDayDialog(int day) {
        LocalDate date = currentMonth.atDay(day);
        DayDialog dialog = new DayDialog(this, date, moodManager);
        dialog.setVisible(true);
        drawCalendar(); // refresh after saving
    }


    public static void main(String[] args) {
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

        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override public void run() {
                splash.setVisible(false);
                splash.dispose();
                SwingUtilities.invokeLater(() -> new LoginScreen());
            }
        }, 3000);
    }
}
