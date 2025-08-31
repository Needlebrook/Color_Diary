import javax.swing.*;
import java.sql.*;

public class LoginScreen extends JFrame {
    private JPasswordField passwordField;
    private final String APP_PASSWORD = "password";  
    private static final String DB_URL = "jdbc:mysql://localhost:3306/color_diary";
    private static final String DB_USER = "celia";
    private static final String DB_PASS = "tiger";

    public LoginScreen() {
    	
        setTitle("Login - Color Diary");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); //closes login window

        JLabel passLabel = new JLabel("Enter Password:");
        passwordField = new JPasswordField(15);
        JButton loginBtn = new JButton("Login");

        loginBtn.addActionListener(e -> {
            String entered = new String(passwordField.getPassword());

            if (entered.equals(APP_PASSWORD)) {
                JOptionPane.showMessageDialog(this, "Welcome!");
                //after login successfully:
                try {
                    Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                    new ColorDiary();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Database connection failed:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }


                dispose(); //close login window
                SwingUtilities.invokeLater(() -> new ColorDiary()); 
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Incorrect password!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                passwordField.setText(""); 
            }
        });

        JPanel panel = new JPanel();
        panel.add(passLabel);
        panel.add(passwordField);
        panel.add(loginBtn);

        add(panel);
        setVisible(true);
    }
}
