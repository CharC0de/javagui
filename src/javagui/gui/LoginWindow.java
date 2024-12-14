package javagui.gui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;

public class LoginWindow extends JFrame {
    public LoginWindow() {
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Create the main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
        add(mainPanel);

        // Title Label
        JLabel lblTitle = new JLabel("Login to Your Account", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(new Color(34, 45, 65));
        GridBagConstraints gbcTitle = new GridBagConstraints();
        gbcTitle.gridx = 0;
        gbcTitle.gridy = 0;
        gbcTitle.gridwidth = 2; // Span across 2 columns
        gbcTitle.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(lblTitle, gbcTitle);

        // Username Label
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        GridBagConstraints gbcUsernameLabel = new GridBagConstraints();
        gbcUsernameLabel.gridx = 0;
        gbcUsernameLabel.gridy = 1;
        gbcUsernameLabel.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(lblUsername, gbcUsernameLabel);

        // Username Text Field
        JTextField txtUsername = new JTextField();
        GridBagConstraints gbcUsernameField = new GridBagConstraints();
        gbcUsernameField.gridx = 1;
        gbcUsernameField.gridy = 1;
        gbcUsernameField.fill = GridBagConstraints.HORIZONTAL;
        gbcUsernameField.weightx = 1.0;
        gbcUsernameField.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(txtUsername, gbcUsernameField);

        // Password Label
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        GridBagConstraints gbcPasswordLabel = new GridBagConstraints();
        gbcPasswordLabel.gridx = 0;
        gbcPasswordLabel.gridy = 2;
        gbcPasswordLabel.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(lblPassword, gbcPasswordLabel);

        // Password Field
        JPasswordField txtPassword = new JPasswordField();
        GridBagConstraints gbcPasswordField = new GridBagConstraints();
        gbcPasswordField.gridx = 1;
        gbcPasswordField.gridy = 2;
        gbcPasswordField.fill = GridBagConstraints.HORIZONTAL;
        gbcPasswordField.weightx = 1.0;
        gbcPasswordField.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(txtPassword, gbcPasswordField);

        // Login Button
        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(34, 45, 65));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        GridBagConstraints gbcLoginButton = new GridBagConstraints();
        gbcLoginButton.gridx = 0;
        gbcLoginButton.gridy = 3;
        gbcLoginButton.gridwidth = 2; // Span across 2 columns
        gbcLoginButton.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(btnLogin, gbcLoginButton);

        // Register Label with a clickable option
        JLabel lblRegister = new JLabel("Don't have an account? Register here.");
        lblRegister.setFont(new Font("Arial", Font.ITALIC, 12));
        lblRegister.setForeground(new Color(0, 120, 215));
        lblRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        GridBagConstraints gbcRegisterLabel = new GridBagConstraints();
        gbcRegisterLabel.gridx = 0;
        gbcRegisterLabel.gridy = 4;
        gbcRegisterLabel.gridwidth = 2; // Span across 2 columns
        gbcRegisterLabel.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(lblRegister, gbcRegisterLabel);

        // Login Button Action Listener
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());

            int userId = validateLogin(username, password);
            if (userId > 0) {
                JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                new DashboardWindow(userId).setVisible(true); // Open Dashboard
                dispose(); // Close LoginWindow
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Register Label Click Action Listener
        lblRegister.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new RegisterWindow().setVisible(true);
                dispose(); // Close the login window
            }
        });
    }

    private int validateLogin(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, password FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    return rs.getInt("id"); // Return user ID
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if login fails
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
    }
}
