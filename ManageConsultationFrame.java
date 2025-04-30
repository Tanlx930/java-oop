import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ManageConsultationFrame extends JFrame {
    private JComboBox<String> declineDateComboBox;
    private JComboBox<String> declineTimeSlotComboBox;
    private JButton declineConfirmButton;
    private JButton declineCancelButton;

    public ManageConsultationFrame(String username) {
        setTitle("Manage Your Consultations");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainContentContainer = new JPanel(new BorderLayout());
        mainContentContainer.setOpaque(false);
        mainContentContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sidebarPanel = createSidebarPanel(username);
        JScrollPane tableScrollPane = createConsultationsTableFromFile("appointment.txt", username);
        JPanel whiteFramePanel = createWhiteFramePanel("Manage Your Consultations", tableScrollPane);

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
            if ("Manage Consultation".equals(option)) {
                button.setBackground(new Color(39, 174, 96)); // Highlight current page
            }

            button.addActionListener(_ -> {
                switch (option) {
                    case "Manage Consultation Slot":
                        dispose();
                        new ManageConsultationSlotFrame(username).setVisible(true);
                        break;
                    case "Consultation Feedback":
                        dispose();
                        new ConsultationFeedbackFrame(username).setVisible(true);
                        break;
                    case "Logout":
                        dispose();
                        new LoginFrame();
                        break;
                }
            });

            sidebarPanel.add(button);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacing between buttons
        }

        return sidebarPanel;
    }

    private JButton createSidebarButton(String text) {
        return new JButton(text) {{
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(250, 60));
            setBackground(new Color(41, 128, 185));
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 14));
            setUI(new RoundedButtonUI());
        }};
    }

    private JScrollPane createConsultationsTableFromFile(String filePath, String username) {
        String[] columnNames = {"Student", "Date", "Time Slot", "Status", "Actions"};
        List<Object[]> finalData = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            // Skip header row if it exists
            if (scanner.hasNextLine()) {
                String header = scanner.nextLine();
                if (!header.startsWith("Lecturer,Date,TimeSlot,Student,Status")) {
                    // Optional: Log a warning if the header format is unexpected
                    System.out.println("Warning: The header format is unexpected.");
                }
            }

            // Process file line by line
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String lecturerUsername = parts[0].trim();
                    String date = parts[1].trim();
                    String timeSlot = parts[2].trim();
                    String studentUsername = parts[3].trim();
                    String status = parts[4].trim();

                    // Add only consultations for the given username
                    if (lecturerUsername.equals(username)) {
                        finalData.add(new Object[]{studentUsername, date, timeSlot, status, "Actions"});
                    }
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error: Appointment file not found.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reading appointment file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Create table model and JTable
        DefaultTableModel tableModel = new DefaultTableModel(finalData.toArray(new Object[0][]), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only the "Actions" column is editable
            }
        };

        JTable consultationsTable = new JTable(tableModel);
        consultationsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        consultationsTable.setRowHeight(40);

        // Customize column widths
        TableColumnModel columnModel = consultationsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100); // Student
        columnModel.getColumn(1).setPreferredWidth(80);  // Date
        columnModel.getColumn(2).setPreferredWidth(80);  // Time Slot
        columnModel.getColumn(3).setPreferredWidth(100); // Status
        columnModel.getColumn(4).setPreferredWidth(200); // Actions

        consultationsTable.getTableHeader().setReorderingAllowed(false);

        // Add action buttons for each row
        consultationsTable.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());
        consultationsTable.getColumnModel().getColumn(4).setCellEditor(new ActionButtonEditor(new JCheckBox(), consultationsTable, this, username));

        return new JScrollPane(consultationsTable);
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

    // Method to remove consultation from file
    private boolean removeConsultationFromFile(String username, String date, String timeSlot, String studentUsername) {
        File inputFile = new File("appointment.txt");
        File tempFile = new File("appointment_temp.txt");

        boolean recordFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            // Read and write the header
            String header = reader.readLine();
            writer.write(header);  // Write header to temp file
            writer.newLine();  // New line after header

            // Process each line in the input file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String fileLecturer = parts[0].trim();
                    String fileDate = parts[1].trim();
                    String fileTimeSlot = parts[2].trim();
                    String fileStudentUsername = parts[3].trim();

                    if (fileLecturer.equalsIgnoreCase(username.trim()) &&
                        fileDate.equalsIgnoreCase(date.trim()) &&
                        fileTimeSlot.equalsIgnoreCase(timeSlot.trim()) &&
                        fileStudentUsername.equalsIgnoreCase(studentUsername.trim())) {

                        recordFound = true;
                    } else {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating the consultation database.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (recordFound) {
            if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
                JOptionPane.showMessageDialog(this, "Error updating the consultation file.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            tempFile.delete();
            JOptionPane.showMessageDialog(this, "No matching consultation found to cancel.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        return recordFound;
    }

    // Method to update the consultation status in the file
    private boolean updateConsultationStatus(String username, String oldDate, String oldTimeSlot, String status, String studentUsername, String newDate, String newTimeSlot) {
        File inputFile = new File("appointment.txt");
        File tempFile = new File("appointment_temp.txt");

        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            // Read and write the header
            String header = reader.readLine();
            writer.write(header);  // Write header to temp file
            writer.newLine();  // New line after header

            // Process each line in the input file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 &&
                    parts[0].equals(username) &&
                    parts[1].equals(oldDate) &&
                    parts[2].equals(oldTimeSlot) &&
                    parts[3].equals(studentUsername)) {

                    // Update the consultation line
                    writer.write(String.join(",", parts[0], newDate, newTimeSlot, parts[3], status));
                    updated = true;
                } else {
                    // Write the unchanged line to the temp file
                    writer.write(line);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating the consultation file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (updated) {
            if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
                JOptionPane.showMessageDialog(this, "Error replacing the original appointment file.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            tempFile.delete();
            JOptionPane.showMessageDialog(this, "No matching consultation found to update.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        return updated;
    }

    

    // ActionButtonRenderer modified to show appropriate buttons based on status
    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll();
            String status = (String) table.getValueAt(row, 3);

            // Add buttons based on the status
            if ("Booked".equalsIgnoreCase(status)) {
                add(createActionButton("Cancel", new Color(231, 76, 60))); // Cancel button for booked
                add(createActionButton("Done", new Color(46, 204, 113))); // Done button
            } else if ("Rescheduling".equalsIgnoreCase(status)) {
                add(createActionButton("Approve", new Color(39, 174, 96)));  // Approve button for rescheduling
                add(createActionButton("Decline", new Color(231, 76, 60)));  // Decline button for rescheduling
            }
            return this;
        }

        private JButton createActionButton(String text, Color color) {
            JButton button = new JButton(text);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            return button;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        private final JTable table;
        private final ManageConsultationFrame parentFrame;
        private final String username; // Store lecturerUsername locally
    
        public ActionButtonEditor(JCheckBox checkBox, JTable table, ManageConsultationFrame parentFrame, String username) {
            super(checkBox);
            this.table = table;
            this.parentFrame = parentFrame;
            this.username = username;
        }
    
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            panel.removeAll();
            String status = (String) table.getValueAt(row, 3);
    
            // Add buttons based on the status
            if ("Booked".equalsIgnoreCase(status)) {
                panel.add(createActionButton("Cancel", new Color(231, 76, 60))); // Cancel button for booked
                panel.add(createActionButton("Done", new Color(231, 76, 60))); // Cancel button for booked
            } else if ("Rescheduling".equalsIgnoreCase(status)) {
                panel.add(createActionButton("Approve", new Color(39, 174, 96)));  // Approve button for rescheduling
                panel.add(createActionButton("Decline", new Color(231, 76, 60)));  // Decline button for rescheduling
            }
            return panel;
        }
    
        private JButton createActionButton(String text, Color color) {
            JButton button = new JButton(text);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.addActionListener(_ -> handleAction(text, table.getSelectedRow()));
            return button;
        }
    
        private void handleAction(String action, int row) {
            if (row < 0) return;
    
            String date = (String) table.getValueAt(row, 1);
            String timeSlot = (String) table.getValueAt(row, 2);
            String studentUsername = (String) table.getValueAt(row, 0); // Adjusted for student username
    
            if ("Cancel".equals(action)) {
                if (JOptionPane.showConfirmDialog(parentFrame, "Confirm cancel?", "Cancel Consultation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (parentFrame.removeConsultationFromFile(username, date, timeSlot, studentUsername)) {
                        ((DefaultTableModel) table.getModel()).removeRow(row);
                        JOptionPane.showMessageDialog(parentFrame, "Consultation cancelled.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, "Failed to cancel consultation.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if ("Approve".equals(action)) {
                if (parentFrame.updateConsultationStatus(username, date, timeSlot, "Booked", studentUsername, date, timeSlot)) {
                    table.setValueAt("Booked", row, 3);  // Update status to "Booked"
                    JOptionPane.showMessageDialog(parentFrame, "Consultation approved and booked.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            
            } else if ("Done".equals(action)) {
                if (parentFrame.updateConsultationStatus(username, date, timeSlot, "done", studentUsername, date, timeSlot)) {
                    table.setValueAt("done", row, 3);  // Update status to "Done"
                    JOptionPane.showMessageDialog(parentFrame, "Status updated to Done.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if ("Decline".equals(action)) {
                showDeclinePanel();
            }
            fireEditingStopped();
        }
    
        private void showDeclinePanel() {
            // Create a new JDialog for the decline panel
            JDialog declineDialog = new JDialog(parentFrame, "Decline Consultation", true); // Modal dialog
            declineDialog.setSize(500, 300);
            declineDialog.setLocationRelativeTo(parentFrame);
            declineDialog.setLayout(new GridLayout(3, 2, 10, 10)); // Add spacing
        
            // Add components to the dialog
            declineDialog.add(new JLabel("Select New Date:"));
            declineDateComboBox = new JComboBox<>(getAvailableDatesFromFile("appointmentSlot.txt"));
            declineDialog.add(declineDateComboBox);
        
            declineDialog.add(new JLabel("Select New Time Slot:"));
            declineTimeSlotComboBox = new JComboBox<>();
            declineDialog.add(declineTimeSlotComboBox);
        
            // Populate the time slots for the first date
            if (declineDateComboBox.getItemCount() > 0) {
                declineDateComboBox.setSelectedIndex(0);
                updateTimeSlotsForSelectedDate();
            }
        
            // Update the time slots when a new date is selected
            declineDateComboBox.addActionListener(_ -> updateTimeSlotsForSelectedDate());
        
            // Use consistent button design
            declineConfirmButton = createStyledButton("Confirm", new Color(39, 174, 96)); // Green for confirm
            declineCancelButton = createStyledButton("Cancel", new Color(231, 76, 60));  // Red for cancel
            declineDialog.add(declineConfirmButton);
            declineDialog.add(declineCancelButton);
        
            // Add action listeners for buttons
            declineConfirmButton.addActionListener(_ -> {
                handleDecline();
                declineDialog.dispose(); // Close the dialog after confirming
            });
        
            declineCancelButton.addActionListener(_ -> declineDialog.dispose()); // Close the dialog without action
        
            // Show the dialog
            declineDialog.setVisible(true);
        }
        // Helper method to fetch available dates from the file
        private String[] getAvailableDatesFromFile(String filePath) {
            Set<String> availableDates = new HashSet<>();
            try (Scanner scanner = new Scanner(new File(filePath))) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");
                    if (parts.length == 4 && parts[0].equals(username)) { // Match lecturerUsername
                        availableDates.add(parts[1].trim()); // Add date
                    }
                }
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(parentFrame, "Error: Appointment slot file not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return availableDates.toArray(new String[0]); // Convert Set to Array
        }
    
        // Helper method to update time slots based on the selected date
        private void updateTimeSlotsForSelectedDate() {
            String selectedDate = (String) declineDateComboBox.getSelectedItem();
            if (selectedDate != null) {
                // Fetch and display the available time slots for the selected date
                String[] timeSlots = getAvailableTimeSlotsForDate("appointmentSlot.txt", selectedDate);
                declineTimeSlotComboBox.removeAllItems(); // Clear previous items
                for (String timeSlot : timeSlots) {
                    declineTimeSlotComboBox.addItem(timeSlot); // Add new time slots
                }
        
                if (timeSlots.length == 0) {
                    JOptionPane.showMessageDialog(parentFrame, "No available time slots for the selected date.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    
        // Helper method to fetch available time slots for a specific date from the file
        private String[] getAvailableTimeSlotsForDate(String filePath, String selectedDate) {
            List<String> availableTimeSlots = new ArrayList<>();
            try (Scanner scanner = new Scanner(new File(filePath))) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");
                    if (parts.length == 4 && parts[0].equals(username) && parts[1].equals(selectedDate)) { // Match lecturerUsername and date
                        String timeStart = parts[2].trim();
                        String timeEnd = parts[3].trim();
                        availableTimeSlots.addAll(splitTimeSlots(timeStart, timeEnd)); // Split the time range into half-hour intervals
                    }
                }
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(parentFrame, "Error: Appointment slot file not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return availableTimeSlots.toArray(new String[0]); // Convert List to Array
        }
    
        // Helper method to split time slots into half-hour intervals
        private List<String> splitTimeSlots(String timeStart, String timeEnd) {
            List<String> slots = new ArrayList<>();
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                Date start = timeFormat.parse(timeStart);
                Date end = timeFormat.parse(timeEnd);
                Calendar calendar = Calendar.getInstance();
    
                calendar.setTime(start);
                while (calendar.getTime().before(end)) {
                    Date slotStart = calendar.getTime();
                    calendar.add(Calendar.MINUTE, 30); // Add 30 minutes
                    Date slotEnd = calendar.getTime();
                    if (slotEnd.after(end)) {
                        slotEnd = end; // Ensure the last slot ends at the correct time
                    }
                    slots.add(timeFormat.format(slotStart) + " - " + timeFormat.format(slotEnd));
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(parentFrame, "Error parsing time: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return slots;
        }
    
        // Helper method for creating styled buttons
        private JButton createStyledButton(String text, Color color) {
            JButton button = new JButton(text);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(100, 40));
            return button;
        }
    
        private void handleDecline() {
            String newDate = (String) declineDateComboBox.getSelectedItem();
            String newTimeSlot = (String) declineTimeSlotComboBox.getSelectedItem();
        
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(parentFrame, "No consultation selected for decline.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            String studentUsername = (String) table.getValueAt(row, 0);
            String date = (String) table.getValueAt(row, 1);
            String timeSlot = (String) table.getValueAt(row, 2);
        
            if (parentFrame.updateConsultationStatus(username, date, timeSlot, "Booked", studentUsername, newDate, newTimeSlot)) {
                JOptionPane.showMessageDialog(parentFrame, "Consultation declined. The new date and time slot are updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new ManageConsultationFrame(username).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Failed to update the consultation status. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
    }
    
}
