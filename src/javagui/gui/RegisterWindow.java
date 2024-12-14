package javagui.gui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterWindow extends JFrame {
    public RegisterWindow() {
        setTitle("Register");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
        add(mainPanel);

        // Title Label
        JLabel lblTitle = new JLabel("Create a New Account", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(new Color(34, 45, 65));
        GridBagConstraints gbcTitle = new GridBagConstraints();
        gbcTitle.gridx = 0;
        gbcTitle.gridy = 0;
        gbcTitle.gridwidth = 2; // Span across 2 columns
        gbcTitle.insets = new Insets(10, 10, 20, 10);
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

        // Confirm Password Label
        JLabel lblConfirmPassword = new JLabel("Confirm Password:");
        lblConfirmPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        GridBagConstraints gbcConfirmPasswordLabel = new GridBagConstraints();
        gbcConfirmPasswordLabel.gridx = 0;
        gbcConfirmPasswordLabel.gridy = 3;
        gbcConfirmPasswordLabel.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(lblConfirmPassword, gbcConfirmPasswordLabel);

        // Confirm Password Field
        JPasswordField txtConfirmPassword = new JPasswordField();
        GridBagConstraints gbcConfirmPasswordField = new GridBagConstraints();
        gbcConfirmPasswordField.gridx = 1;
        gbcConfirmPasswordField.gridy = 3;
        gbcConfirmPasswordField.fill = GridBagConstraints.HORIZONTAL;
        gbcConfirmPasswordField.weightx = 1.0;
        gbcConfirmPasswordField.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(txtConfirmPassword, gbcConfirmPasswordField);

        // Register Button
        JButton btnRegister = new JButton("Register");
        btnRegister.setBackground(new Color(34, 45, 65));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setFont(new Font("Arial", Font.BOLD, 14));
        GridBagConstraints gbcRegisterButton = new GridBagConstraints();
        gbcRegisterButton.gridx = 0;
        gbcRegisterButton.gridy = 4;
        gbcRegisterButton.gridwidth = 2; // Span across 2 columns
        gbcRegisterButton.insets = new Insets(20, 10, 10, 10);
        mainPanel.add(btnRegister, gbcRegisterButton);

        // Back Button
        JButton btnBack = new JButton("Back");
        btnBack.setBackground(Color.LIGHT_GRAY);
        btnBack.setFocusPainted(false);
        btnBack.setFont(new Font("Arial", Font.PLAIN, 12));
        GridBagConstraints gbcBackButton = new GridBagConstraints();
        gbcBackButton.gridx = 0;
        gbcBackButton.gridy = 5;
        gbcBackButton.gridwidth = 2; // Span across 2 columns
        gbcBackButton.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(btnBack, gbcBackButton);

        // Register Button Action Listener
        btnRegister.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());

            if (password.equals(confirmPassword)) {
                if (registerUser(username, password)) {
                    JOptionPane.showMessageDialog(this, "Registration Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    new LoginWindow().setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Registration Failed. Username may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Back Button Action Listener
        btnBack.addActionListener(e -> {
            new LoginWindow().setVisible(true);
            dispose();
        });
    }

    private boolean registerUser(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterWindow().setVisible(true));
    }
}
