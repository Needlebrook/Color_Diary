package com.colordiary;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;

public class StatsManager {
    private MoodManager moodManager;

    public StatsManager(MoodManager moodManager) {
        this.moodManager = moodManager;
    }

    public void showStatsWindow(JFrame parent, YearMonth currentMonth) {
        JDialog statsDialog = new JDialog(parent, "Statistics", true);
        statsDialog.setSize(800, 500);
        statsDialog.setLayout(new BorderLayout());

        // --- Step 1: Get stats from MoodManager/DB ---
        int totalEntries = 0;
        double avgMood = 0.0;

        try {
            ResultSet rs = moodManager.getStatsForMonth(currentMonth);
            if (rs.next()) {
                totalEntries = rs.getInt("total");
                avgMood = rs.getDouble("avg");
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // --- Step 2: Summary labels ---
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1));
        summaryPanel.add(new JLabel("Entries this month: " + totalEntries));
        summaryPanel.add(new JLabel("Average mood: " + String.format("%.2f", avgMood)));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("This Month"));
        for (Component c : summaryPanel.getComponents()) {
            c.setFont(new Font("SansSerif", Font.PLAIN, 14));
        }


        statsDialog.add(summaryPanel, BorderLayout.NORTH);

        // --- Step 3: Mood trend chart ---
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            ResultSet rs = moodManager.getMoodTrend(currentMonth);
            while (rs.next()) {
                int day = rs.getInt("day");
                int rating = rs.getInt("mood_rating");
                dataset.addValue(rating, "Mood", String.valueOf(day));
            }
            rs.close();
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
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));
        chart.setBackgroundPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(new Color(245, 248, 255));


        statsDialog.setLocationRelativeTo(parent);
        statsDialog.setVisible(true);
    }
}
