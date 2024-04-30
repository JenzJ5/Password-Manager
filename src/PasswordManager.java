import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PasswordManager extends JFrame {

    private Connection connection;
    private String username;
    private JTextField websiteField;
    private JPasswordField passwordField;
    private JButton saveButton;
    private JPanel passwordsPanel;

    public PasswordManager(Connection connection, String username) {
        super("Password Manager");
        this.connection = connection;
        this.username = username;
        initComponents();
    }

    private void initComponents() {
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.white);
        getContentPane().add(mainPanel);

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.white);
        mainPanel.add(leftPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        inputPanel.setBackground(Color.white);
        leftPanel.add(inputPanel, BorderLayout.NORTH);

        inputPanel.add(new JLabel("Website:"));
        websiteField = new JTextField();
        websiteField.setPreferredSize(new Dimension(325, 30));
        inputPanel.add(websiteField);

        inputPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(passwordField);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.white);

        saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(120, 40));
        saveButton.setBackground(new Color(59, 89, 182));
        saveButton.setForeground(Color.white);
        saveButton.addActionListener(e -> savePassword());
        bottomPanel.add(saveButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        passwordsPanel = new JPanel();
        passwordsPanel.setBackground(Color.white);
        JScrollPane scrollPane = new JScrollPane(passwordsPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        displayPasswords();
    }

    private void savePassword() {
        String website = websiteField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (website.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out website and password fields.");
            return;
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO accounts (website, username, password) VALUES (?, ?, ?)");
            preparedStatement.setString(1, website);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, password);
            preparedStatement.executeUpdate();
            displayPasswords();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save password.");
        }
    }

    private void displayPasswords() {
        try {
            passwordsPanel.removeAll();

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM accounts WHERE username = ?");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            JPanel passwordListPanel = new JPanel();
            passwordListPanel.setLayout(new BoxLayout(passwordListPanel, BoxLayout.Y_AXIS));

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String website = resultSet.getString("website");
                String password = resultSet.getString("password");

                JPanel passwordPanel = createPasswordPanel(id, website, password);
                passwordListPanel.add(passwordPanel);
            }

            JScrollPane scrollPane = new JScrollPane(passwordListPanel);
            passwordsPanel.add(scrollPane);

            passwordsPanel.revalidate();
            passwordsPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch passwords.");
        }
    }

    private JPanel createPasswordPanel(int id, String website, String password) {
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(Color.white);
        passwordPanel.setPreferredSize(new Dimension(500, 50));
        JLabel infoLabel = new JLabel("Website: " + website + ", Password: " + password);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.white);
        deleteButton.addActionListener(e -> deletePassword(id));

        passwordPanel.add(infoLabel, BorderLayout.CENTER);
        passwordPanel.add(deleteButton, BorderLayout.LINE_END);

        return passwordPanel;
    }

    private void deletePassword(int id) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this password?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM accounts WHERE id = ?");
                preparedStatement.setInt(1, id);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Password deleted successfully.");
                    displayPasswords();
                } else {
                    JOptionPane.showMessageDialog(this, "Password not found.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to delete password.");
            }
        }
    }

    private void clearFields() {
        websiteField.setText("");
        passwordField.setText("");
    }

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/password_manager", "root", "12345");

            SignInFrame signInFrame = new SignInFrame(connection, username -> {
                PasswordManager passwordManager = new PasswordManager(connection, username);
                passwordManager.setVisible(true);
            });
            signInFrame.setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to database.");
            System.exit(1);
        }
    }
}
