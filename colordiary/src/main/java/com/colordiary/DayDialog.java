package com.colordiary;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class DayDialog extends JDialog {
    private JTextField ratingField;
    private JTextField emojiField;
    private JTextArea notesArea;

    public DayDialog(JFrame parent, LocalDate date, MoodManager moodManager) {
        super(parent, "Day Entry - " + date.getDayOfMonth(), true);
        setSize(750, 500);
        setLayout(new BorderLayout(10, 10));

        // --- Top panel (mood + emoji) ---
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JLabel ratingLabel = new JLabel("Mood Rating (1–10):");
        ratingField = new JTextField();
        JLabel emojiLabel = new JLabel("Emoji:");
        emojiField = new JTextField();
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

        JPanel emojiPanel = new JPanel(new BorderLayout());
        emojiPanel.add(emojiField, BorderLayout.CENTER);
        emojiPanel.add(emojiBtn, BorderLayout.EAST);

        topPanel.add(ratingLabel);
        topPanel.add(ratingField);
        topPanel.add(emojiLabel);
        topPanel.add(emojiPanel);

        // --- Notes section ---
        JLabel textLabel = new JLabel("Notes:");
        notesArea = new JTextArea(10, 40);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(notesArea);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(topPanel, BorderLayout.NORTH);
        add(textLabel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Load existing entry ---
        MoodEntry entry = moodManager.getEntryForDate(date);
        if (entry != null) {
            if (entry.rating != null) ratingField.setText(String.valueOf(entry.rating));
            if (entry.emoji != null) emojiField.setText(entry.emoji);
            if (entry.notes != null) notesArea.setText(entry.notes);
        }

        // --- Save button ---
        saveBtn.addActionListener(e -> {
            try {
                int mood = Integer.parseInt(ratingField.getText());
                moodManager.saveEntry(date, mood, emojiField.getText(), notesArea.getText());
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid mood rating. Please enter 1–10.");
            }
        });

        cancelBtn.addActionListener(e -> dispose());

        setLocationRelativeTo(parent);
    }
}
