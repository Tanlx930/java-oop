import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsultationFeedbackFrame extends JFrame {

    public ConsultationFeedbackFrame(String username) {
        setTitle("Consultation Feedback - " + username);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel sidebarPanel = createSidebarPanel(username);
        JScrollPane tableScrollPane = createFeedbackTableFromFile("appointment.txt", username);
        JPanel whiteFramePanel = createWhiteFramePanel("Consultation Feedback Management", tableScrollPane);

        JPanel mainContentContainer = new JPanel(new BorderLayout());
        mainContentContainer.setOpaque(false);
        mainContentContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainContentContainer.add(whiteFramePanel, BorderLayout.CENTER);
        mainContentContainer.add(sidebarPanel, BorderLayout.WEST);

        BackgroundPanel backgroundPanel = new BackgroundPanel("bg.png");
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.add(mainContentContainer, BorderLayout.CENTER);

        add(backgroundPanel);
    }

    private JPanel createSidebarPanel(String username) {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(52, 152, 219, 200));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(250, 20, 20, 20));

        String[] options = {"Manage Consultation Slot", "Manage Consultation", "Consultation Feedback", "Logout"};

        for (String option : options) {
            JButton button = createSidebarButton(option);
            if ("Consultation Feedback".equals(option)) {
                button.setBackground(new Color(39, 174, 96));
            }

            button.addActionListener(_ -> {
                switch (option) {
                    case "Manage Consultation Slot":
                        dispose();
                        new ManageConsultationSlotFrame(username).setVisible(true);
                        break;
                    case "Manage Consultation":
                        dispose();
                        new ManageConsultationFrame(username).setVisible(true);
                        break;
                    case "Logout":
                        dispose();
                        new LoginFrame();
                        break;
                }
            });

            sidebarPanel.add(button);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        return sidebarPanel;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 60));
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setUI(new RoundedButtonUI());
        return button;
    }

    private JScrollPane createFeedbackTableFromFile(String filePath, String username) {
        String[] columnNames = {"Student", "Date", "Slot", "Your Feedback", "Student Feedback", "Action"};
        List<Object[]> finalData = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                if (parts.length >= 5 && parts[0].equals(username) && "done".equalsIgnoreCase(parts[4])) {
                    String student = parts[3];
                    String date = parts[1];
                    String slot = parts[2];
                    String lecturerFeedback = parts.length > 5 ? parts[5] : "";
                    String studentFeedback = parts.length > 6 ? parts[6] : "";

                    finalData.add(new Object[]{student, date, slot, lecturerFeedback, studentFeedback, "Edit"});
                }
            }
        } catch (FileNotFoundException _) {
            JOptionPane.showMessageDialog(this, "Error reading the appointment file.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        finalData.sort((o1, o2) -> {
            int dateComparison = ((String) o1[1]).compareTo((String) o2[1]);
            if (dateComparison != 0) {
                return dateComparison;
            }
            return ((String) o1[0]).compareTo((String) o2[0]);
        });

        DefaultTableModel tableModel = new DefaultTableModel(finalData.toArray(new Object[0][]), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        JTable feedbackTable = new JTable(tableModel);
        feedbackTable.setFont(new Font("Arial", Font.PLAIN, 14));
        feedbackTable.setRowHeight(30);

        feedbackTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        feedbackTable.getColumnModel().getColumn(5).setCellEditor(new ActionButtonEditor(new JCheckBox(), feedbackTable, username, filePath));

        return new JScrollPane(feedbackTable);
    }

    private JPanel createWhiteFramePanel(String title, JScrollPane tableScrollPane) {
        JPanel whiteFramePanel = new JPanel(new BorderLayout());
        whiteFramePanel.setBackground(new Color(255, 255, 255, 200));
        whiteFramePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        whiteFramePanel.add(titleLabel, BorderLayout.NORTH);
        whiteFramePanel.add(tableScrollPane, BorderLayout.CENTER);
        return whiteFramePanel;
    }

    private static class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = new JButton("Edit");
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setBackground(new Color(39, 174, 96));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            return button;
        }
    }

    private static class ActionButtonEditor extends DefaultCellEditor {
        private final JTable table;
        private final String username;
        private final String filePath;

        public ActionButtonEditor(JCheckBox checkBox, JTable table, String username, String filePath) {
            super(checkBox);
            this.table = table;
            this.username = username;
            this.filePath = filePath;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JButton button = new JButton("Edit");
            button.addActionListener(_ -> {
                String currentFeedback = (String) table.getValueAt(row, 3); // Your Feedback column
                String updatedFeedback = JOptionPane.showInputDialog("Edit your feedback:", currentFeedback);

                if (updatedFeedback != null && !updatedFeedback.trim().isEmpty()) {
                    table.setValueAt(updatedFeedback, row, 3);
                    saveFeedbackToFile(row, updatedFeedback);
                }
            });
            return button;
        }

        private void saveFeedbackToFile(int row, String updatedFeedback) {
            String student = (String) table.getValueAt(row, 0);
            String date = (String) table.getValueAt(row, 1);
            String slot = (String) table.getValueAt(row, 2);
        
            File inputFile = new File(filePath);
            File tempFile = new File("appointment_temp.txt");
        
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                String line;
                boolean feedbackUpdated = false;
        
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    
        
                    if (parts.length >= 7) {
                        // Check for a match
                        if (parts[0].equals(username) && parts[1].equals(date) && parts[2].equals(slot) && parts[3].equals(student)) {
                            parts[5] = updatedFeedback; // Update lecturer feedback
                            feedbackUpdated = true;
                            writer.write(String.join(",", parts));

                        } else {
                            writer.write(line); // Write the line unchanged
                        }
                    
                    } else if (parts.length >= 5 && parts[0].equals(username) && parts[1].equals(date) && parts[2].equals(slot) && parts[3].equals(student)) {
                        // Handle cases where the feedback column is initially missing
                        String[] extendedParts = new String[7];
                        System.arraycopy(parts, 0, extendedParts, 0, parts.length);
                        extendedParts[5] = updatedFeedback; // Add student feedback
                        feedbackUpdated = true;
                        writer.write(String.join(",", extendedParts));

                    } else {
                        writer.write(line); // Write the line unchanged
                    }
                    writer.newLine();
                }
        
                if (!feedbackUpdated) {
                    JOptionPane.showMessageDialog(null, "Feedback not saved. Record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error updating feedback: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        
            if (inputFile.delete() && tempFile.renameTo(inputFile)) {
                JOptionPane.showMessageDialog(null, "Feedback updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error replacing the file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
    }

    private static class BackgroundPanel extends JPanel {
        private final Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            backgroundImage = new ImageIcon(imagePath).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
