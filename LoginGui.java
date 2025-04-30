import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;

public class LoginGui {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}

class LoginFrame extends JFrame {
    private final String backgroundImagePath = "bg.png"; // Update this path accordingly

    public LoginFrame() {
        setTitle("Welcome To Psychology Consultation Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Setting up the background
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImagePath);
        backgroundPanel.setLayout(null);

        // White Frame Panel
        JPanel whiteFramePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
            }
        };
        whiteFramePanel.setBackground(Color.WHITE);
        whiteFramePanel.setOpaque(false);
        whiteFramePanel.setSize(400, 400);
        whiteFramePanel.setLocation((getWidth() - whiteFramePanel.getWidth()) / 2, (getHeight() - whiteFramePanel.getHeight()) / 2);
        whiteFramePanel.setLayout(null);
        backgroundPanel.add(whiteFramePanel);

        // Title
        JLabel titleLabel = new JLabel("Consultation Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setBounds(80, 50, 350, 30);
        whiteFramePanel.add(titleLabel);

        // Username Label and Field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        usernameLabel.setBounds(80, 100, 80, 20);
        whiteFramePanel.add(usernameLabel);

        JTextField usernameField = new JTextField(15);
        usernameField.setBounds(80, 120, 250, 30);
        usernameField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        usernameField.setBackground(new Color(230, 230, 250)); // Light violet background
        whiteFramePanel.add(usernameField);

        // Password Label and Field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passwordLabel.setBounds(80, 170, 80, 20);
        whiteFramePanel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setBounds(80, 190, 250, 30);
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        passwordField.setBackground(new Color(230, 230, 250)); // Light violet background
        whiteFramePanel.add(passwordField);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setContentAreaFilled(false);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(false);
        loginButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g, c);
            }
        });
        loginButton.setBounds(80, 240, 250, 30);
        loginButton.setFocusPainted(false);
        loginButton.setBackground(new Color(72, 133, 237));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        whiteFramePanel.add(loginButton);



        // Register Button
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(_ -> {
            dispose();
            new RegisterFrame();
        });
        registerButton.setContentAreaFilled(false);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.setOpaque(false);
        registerButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g, c);
            }
        });
        registerButton.setBounds(80, 290, 250, 30);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setOpaque(false);
        registerButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g, c);
            }
        });
        registerButton.setFocusPainted(false);
        registerButton.setBackground(new Color(76, 175, 80));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        whiteFramePanel.add(registerButton);

        // Adding action listener to login button
        loginButton.addActionListener(_ -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
        
            try {
                File file = new File("user.txt");
                if (!file.exists()) {
                    JOptionPane.showMessageDialog(this, "Database not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
        
                String userRole=null;  // Wrapper for userRole
                boolean[] loginSuccessful = {false}; // Wrapper for loginSuccessful
        
                try (Scanner scanner = new Scanner(file)) {
                    if (scanner.hasNextLine()) {
                        scanner.nextLine(); // Skip the header line
                    }
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] userDetails = line.split(",");
                        if (userDetails.length >= 3) {
                            String dbUsername = userDetails[0].trim();
                            String dbPassword = userDetails[1].trim();
                            String dbRole = userDetails[3].trim();
        
                            if (username.equals(dbUsername) && password.equals(dbPassword)) {
                                loginSuccessful[0] = true;
                                userRole = dbRole;
                                break;
                            }
                        }
                    }
                }
        
                if (loginSuccessful[0]) {         
                    dispose();
                        if ("Lecturer".equalsIgnoreCase(userRole)) {
                            // Open the Consultation Feedback Frame for Lecturer
                            SwingUtilities.invokeLater(() -> new ManageConsultationSlotFrame(username).setVisible(true));
                        } else if ("Student".equalsIgnoreCase(userRole)) {
                            // Open the Homepage Frame for Student
                            SwingUtilities.invokeLater(() -> new BookAppointmentFrame(username).setVisible(true));
                        } else {
                            // Handle unknown userRole
                            JOptionPane.showMessageDialog(null, "Unknown role: " + userRole, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Username or Password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while accessing the database", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                whiteFramePanel.setLocation((getWidth() - whiteFramePanel.getWidth()) / 2, (getHeight() - whiteFramePanel.getHeight()) / 2);
            }
        });

        add(backgroundPanel);
        setVisible(true);
    }
}

class RegisterFrame extends JFrame {
    public RegisterFrame() {
        setTitle("Register - Psychology Consultation Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        BackgroundPanel backgroundPanel = new BackgroundPanel("bg.png");
        backgroundPanel.setLayout(null);

        JPanel whiteFramePanel = createWhiteFramePanel();
        backgroundPanel.add(whiteFramePanel);

        addComponents(whiteFramePanel);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                whiteFramePanel.setLocation((getWidth() - whiteFramePanel.getWidth()) / 2, (getHeight() - whiteFramePanel.getHeight()) / 2);
            }
        });

        add(backgroundPanel);
        setVisible(true);
    }

    private JPanel createWhiteFramePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
            }
        };
        panel.setBackground(Color.WHITE);
        panel.setOpaque(false);
        panel.setSize(500, 500);
        panel.setLocation((getWidth() - panel.getWidth()) / 2, (getHeight() - panel.getHeight()) / 2);
        panel.setLayout(null);
        return panel;
    }

    private void addComponents(JPanel panel) {
        String[][] labels = {
            {"Register", "200, 20, 350, 30", "16", "bold"},
            {"Role:", "130, 70, 80, 20", "12", "plain"},
            {"Username:", "130, 130, 80, 20", "12", "plain"},
            {"Password:", "130, 190, 80, 20", "12", "plain"},
            {"Confirm Password:", "130, 250, 120, 20", "12", "plain"},
            {"Email:", "130, 310, 80, 20", "12", "plain"}
        };

        for (String[] labelData : labels) {
            JLabel label = new JLabel(labelData[0]);
            label.setFont(new Font("Arial", "bold".equals(labelData[3]) ? Font.BOLD : Font.PLAIN, Integer.parseInt(labelData[2])));
            label.setBounds(getBounds(labelData[1]));
            panel.add(label);
        }

        JTextField usernameField = createTextField(130, 150);
        JPasswordField passwordField = createPasswordField(130, 210);
        JPasswordField confirmPasswordField = createPasswordField(130, 270);
        JTextField emailField = createTextField(130, 330);
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{null, "Student", "Lecturer"});
        roleComboBox.setBounds(130, 90, 250, 30);

        panel.add(usernameField);
        panel.add(passwordField);
        panel.add(confirmPasswordField);
        panel.add(emailField);
        panel.add(roleComboBox);

        JButton registerButton = createRegisterButton(roleComboBox, usernameField, passwordField, confirmPasswordField, emailField);
        panel.add(registerButton);

        // Back Button
        JButton backButton = new JButton("Back") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(getForeground());
                FontMetrics fm = g.getFontMetrics();
                int stringWidth = fm.stringWidth(getText());
                int stringHeight = fm.getAscent();
                g2d.drawString(getText(), (getWidth() - stringWidth) / 2, (getHeight() + stringHeight) / 2);
            }
        };
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setBounds(130, 430, 250, 40);
        backButton.setBackground(new Color(72, 133, 237));
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Arial", Font.BOLD, 14));

        backButton.addActionListener(_ -> {
            dispose();
            new LoginFrame();
        });
        panel.add(backButton);
    }

    private JTextField createTextField(int x, int y) {
        JTextField textField = new JTextField(15);
        textField.setBounds(x, y, 250, 30);
        textField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        textField.setBackground(new Color(230, 230, 250));
        return textField;
    }

    private JPasswordField createPasswordField(int x, int y) {
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setBounds(x, y, 250, 30);
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        passwordField.setBackground(new Color(230, 230, 250));
        return passwordField;
    }

    private JButton createRegisterButton(JComboBox<String> roleComboBox, JTextField usernameField, JPasswordField passwordField, JPasswordField confirmPasswordField, JTextField emailField) {
        JButton button = new JButton("Register") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(getForeground());
                FontMetrics fm = g.getFontMetrics();
                int stringWidth = fm.stringWidth(getText());
                int stringHeight = fm.getAscent();
                g2d.drawString(getText(), (getWidth() - stringWidth) / 2, (getHeight() + stringHeight) / 2);
            }
        };
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setBounds(130, 380, 250, 40);
        button.setBackground(new Color(76, 175, 80));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));

        button.addActionListener(_ -> {
            String role = (String) roleComboBox.getSelectedItem();
            boolean registrationSuccessful = false;
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText();

            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            Pattern pattern = Pattern.compile(emailRegex);
            Matcher matcher = pattern.matcher(email);

            if (role == null) {
                JOptionPane.showMessageDialog(this, "Please select a role", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Confirm Password cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!matcher.matches()) {
                JOptionPane.showMessageDialog(this, "Invalid email format", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    File file = new File("user.txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    boolean usernameExists = false;
                    boolean emailExists = false;
                    try (Scanner scanner = new Scanner(file)) {
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            if (line.contains(username)) {
                                usernameExists = true;
                            } else if (line.contains(email)) {
                                emailExists = true;
                            }
                        }
                    }

                    if (usernameExists) {
                        JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                    } else if (emailExists) {
                        JOptionPane.showMessageDialog(this, "Email already exists", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        try (FileWriter writer = new FileWriter(file, true)) {
                            writer.write(username + "," + password + "," + email + "," + role + "\n");
                        }
                        JOptionPane.showMessageDialog(this, "Registration Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                registrationSuccessful = true;
                if (registrationSuccessful) {
                    dispose();
                    new LoginFrame();
                }
                    }
                } catch (IOException ex) {
                ex.printStackTrace();
                                if (registrationSuccessful) {
                    dispose();
                    new LoginFrame();
                }
                }
            }
        });
        return button;
    }

    private Rectangle getBounds(String bounds) {
        String[] parts = bounds.split(", ");
        return new Rectangle(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }
}

class BackgroundPanel extends JPanel {
    private final Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            backgroundImage = new ImageIcon(imagePath).getImage();
        } else {
            backgroundImage = null;
            System.err.println("Error: Background image not found at " + imagePath);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
