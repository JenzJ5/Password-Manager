import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.function.Consumer;

public class SignInFrame extends JFrame {

    private Connection connection;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Consumer<String> loginHandler;
    private JButton registerButton;

    public SignInFrame(Connection connection, Consumer<String> loginHandler) {
        super("Sign In");
        this.connection = connection;
        this.loginHandler = loginHandler;
        initComponents();
    }

    private void initComponents() {
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 5));
        getContentPane().add(panel);

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton signInButton = new JButton("Sign In");
        signInButton.addActionListener(e -> signIn());
        panel.add(signInButton);

        registerButton = new JButton("Register");
        registerButton.addActionListener(e -> register());
        panel.add(registerButton);
    }

    private void signIn() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                loginHandler.accept(username); 
                dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while signing in.");
        }
    }

    private void register() {
        RegisterFrame registerFrame = new RegisterFrame(connection);
        registerFrame.setVisible(true);
    }
}
