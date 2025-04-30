import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class ManageAppointmentFrame extends JFrame {

    public ManageAppointmentFrame(String username) {
        setTitle("Manage Appointments");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainContentContainer = new JPanel(new BorderLayout());
        mainContentContainer.setOpaque(false);
        mainContentContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sidebarPanel = createSidebarPanel(username);
        JScrollPane tableScrollPane = createAppointmentsTableFromFile("appointment.txt", username);
        JPanel whiteFramePanel = createWhiteFramePanel("Manage Your Appointments", tableScrollPane);

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

        // Define sidebar options based on the user role
        String[] options = {"Book Appointment", "Manage Appointment", "Appointment Feedback", "Logout"};

        for (String option : options) {
            JButton button = createSidebarButton(option);
            if ("Manage Appointment".equals(option)) {
                button.setBackground(new Color(39, 174, 96)); // Highlight the current page
            }

            button.addActionListener(_ -> {
                switch (option) {
                    case "Book Appointment":
                        dispose();
                        new BookAppointmentFrame(username).setVisible(true);
                        break;
                    case "Appointment Feedback":
                        dispose();
                        new AppointmentFeedbackFrame(username).setVisible(true);
                        break;
                    case "Logout":
                        dispose();
                        new LoginGui();
                        break;
                }
            });

            sidebarPanel.add(button);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacing between buttons
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

    private JScrollPane createAppointmentsTableFromFile(String filePath, String username) {
        String[] columnNames = {"Lecturer", "Date", "Time Slot", "Status", "Actions"};
        List<Object[]> finalData = new ArrayList<>();
    
        try (Scanner scanner = new Scanner(new File(filePath))) {
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Skip the first line (header)
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 5 && !parts[4].equalsIgnoreCase("done")) {
                    finalData.add(new Object[]{parts[0], parts[1], parts[2], parts[4], "Actions"});
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading the appointment file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    
        DefaultTableModel tableModel = new DefaultTableModel(finalData.toArray(new Object[0][]), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only the "Actions" column is editable
            }
        };
    
        JTable appointmentsTable = new JTable(tableModel);
        appointmentsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        appointmentsTable.setRowHeight(40); // Adjust row height for better visibility
    
        // Set specific column widths
        TableColumnModel columnModel = appointmentsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100); // Lecturer
        columnModel.getColumn(1).setPreferredWidth(80);  // Date
        columnModel.getColumn(2).setPreferredWidth(80);  // Time Slot
        columnModel.getColumn(3).setPreferredWidth(100); // Status
        columnModel.getColumn(4).setPreferredWidth(200); // Actions
    
        appointmentsTable.getTableHeader().setReorderingAllowed(false);
    
        appointmentsTable.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());
            appointmentsTable.getColumnModel().getColumn(4).setCellEditor(new ActionButtonEditor(new JCheckBox(), appointmentsTable, this,username));
    
        return new JScrollPane(appointmentsTable);
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
}

class ActionButtonRenderer extends JPanel implements TableCellRenderer {
    public ActionButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        removeAll();
        String status = (String) table.getValueAt(row, 3);

        // Add button based on the status
        if ("done".equalsIgnoreCase(status)) {
            add(createActionButton("Feedback", new Color(39, 174, 96)));
        } else {
            if (!"rescheduling".equalsIgnoreCase(status)) {
                add(createActionButton("Reschedule", new Color(41, 128, 185)));
            }
            add(createActionButton("Cancel", new Color(231, 76, 60)));
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
    private final ManageAppointmentFrame parentFrame;
    private final String username;

    public ActionButtonEditor(JCheckBox checkBox, JTable table, ManageAppointmentFrame parentFrame, String username) {
        super(checkBox);
        this.table = table;
        this.parentFrame = parentFrame;
        this.username = username;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        panel.removeAll();
        String status = (String) table.getValueAt(row, 3);

        // Add appropriate buttons based on the status
        if ("done".equalsIgnoreCase(status)) {
            panel.add(createActionButton("Feedback"));
        } else {
            if (!"rescheduling".equalsIgnoreCase(status)) {
                panel.add(createActionButton("Reschedule"));
            }
            panel.add(createActionButton("Cancel"));
        }
        return panel;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.addActionListener(_ -> handleAction(text, table.getSelectedRow(), username));
        return button;
    }

    private void handleAction(String action, int row, String username) {
        if (row < 0) return;
    
        String lecturer = (String) table.getValueAt(row, 0);
        String date = (String) table.getValueAt(row, 1);
        String timeSlot = (String) table.getValueAt(row, 2);
    
        if ("Cancel".equals(action)) {
            // Existing Cancel logic
            if (JOptionPane.showConfirmDialog(parentFrame, "Confirm cancel?", "Cancel Appointment", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (removeAppointmentFromFile(lecturer, date, timeSlot, username)) {
                    ((DefaultTableModel) table.getModel()).removeRow(row);
                    JOptionPane.showMessageDialog(parentFrame, "Appointment cancelled.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Failed to cancel appointment.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if ("Reschedule".equals(action)) {
            // Existing Reschedule logic
            new RescheduleDialog(parentFrame, lecturer,username).setVisible(true);
        } else if ("Feedback".equals(action)) {
            // Handle Feedback
            handleFeedback(lecturer, date, timeSlot, username, row);
        }
        fireEditingStopped();
    }
    
    private boolean removeAppointmentFromFile(String lecturer, String date, String timeSlot, String username) {
        File inputFile = new File("appointment.txt");
        File tempFile = new File("appointment_temp.txt");

        boolean recordFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String fileLecturer = parts[0].trim();
                    String fileDate = parts[1].trim();
                    String fileTimeSlot = parts[2].trim();
                    String fileStudentUsername = parts[3].trim();

                    if (fileLecturer.equalsIgnoreCase(lecturer.trim()) && fileDate.equals(date.trim()) &&
                            fileTimeSlot.equals(timeSlot.trim()) && fileStudentUsername.equalsIgnoreCase(username.trim())) {
                        recordFound = true;
                    } else {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentFrame, "Error updating the appointment database.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (recordFound) {
            if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
                JOptionPane.showMessageDialog(parentFrame, "Error updating the appointment file.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            tempFile.delete();
            JOptionPane.showMessageDialog(parentFrame, "No matching appointment found to cancel.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        return recordFound;
    }
    private void handleFeedback(String lecturer, String date, String timeSlot, String username, int row) {
        // Open a dialog to get the feedback
        String currentFeedback = (String) table.getValueAt(row, 5); // Assuming feedback is stored in column 5
        String feedback = JOptionPane.showInputDialog(parentFrame, "Enter your feedback:", "Feedback", JOptionPane.PLAIN_MESSAGE);
    
        if (feedback != null) {
            // Save feedback to file
            if (updateFeedbackInFile(lecturer, date, timeSlot, username, feedback)) {
                JOptionPane.showMessageDialog(parentFrame, "Feedback saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                table.setValueAt(feedback, row, 5); // Update the table with the new feedback
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Failed to save feedback.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private boolean updateFeedbackInFile(String lecturer, String date, String timeSlot, String username, String feedback) {
        File inputFile = new File("appointment.txt");
        File tempFile = new File("appointment_temp.txt");
        boolean updated = false;
    
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 &&
                    parts[0].equals(lecturer) &&
                    parts[1].equals(date) &&
                    parts[2].equals(timeSlot) &&
                    parts[3].equals(username)) {
                    // Update the feedback field
                    writer.write(String.join(",", parts[0], parts[1], parts[2], parts[3], parts[4], feedback));
                    updated = true;
                } else {
                    // Write the line unchanged
                    writer.write(line);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentFrame, "Error updating the appointment file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    
        // Replace the original file with the updated file
        if (updated) {
            if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
                JOptionPane.showMessageDialog(parentFrame, "Error updating the file.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            tempFile.delete(); // Clean up the temp file if no changes were made
        }
    
        return updated;
    }
    
}

class RescheduleDialog extends JDialog {
    private JComboBox<String> dateComboBox;
    private JComboBox<String> timeSlotComboBox;
    private JButton confirmButton;
    private JButton cancelButton;
    private String lecturer;
    private ManageAppointmentFrame parentFrame;

    public RescheduleDialog(ManageAppointmentFrame parentFrame, String lecturer, String username) {
        this.parentFrame = parentFrame;
        this.lecturer = lecturer;

        setTitle("Reschedule Appointment");
        setSize(400, 300);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());
        setModal(true);

        JPanel datePanel = new JPanel(new FlowLayout());
        datePanel.add(new JLabel("Select Date:"));
        dateComboBox = new JComboBox<>(getAvailableDates());
        datePanel.add(dateComboBox);

        JPanel timeSlotPanel = new JPanel(new FlowLayout());
        timeSlotPanel.add(new JLabel("Select Time Slot:"));
        timeSlotComboBox = new JComboBox<>(getAvailableTimeSlots());
        timeSlotPanel.add(timeSlotComboBox);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        confirmButton = new JButton("Confirm");
        cancelButton = new JButton("Cancel");
        buttonsPanel.add(confirmButton);
        buttonsPanel.add(cancelButton);

        add(datePanel, BorderLayout.NORTH);
        add(timeSlotPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        confirmButton.addActionListener(_ -> confirmReschedule(username));
        cancelButton.addActionListener(_ -> dispose());
        dateComboBox.addActionListener(_ -> updateTimeSlots(username));
    }

    private String[] getAvailableDates() {
        List<LocalDate> availableDates = new ArrayList<>();
        Set<String> allAvailableDates = new HashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Scanner scanner = new Scanner(new File("appointmentslot.txt"))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 4 && parts[0].equals(lecturer)) {
                    allAvailableDates.add(parts[1]);
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading the appointment slots database.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        Set<String> bookedDates = getBookedDates();
        for (String date : allAvailableDates) {
            if (hasAvailableSlots(date)) {
                availableDates.add(LocalDate.parse(date, formatter));
            }
        }

        Collections.sort(availableDates);
        List<String> sortedDates = new ArrayList<>();
        for (LocalDate date : availableDates) {
            sortedDates.add(date.format(formatter));
        }

        return sortedDates.toArray(new String[0]);
    }

    private String[] getAvailableTimeSlots() {
        List<String> availableTimeSlots = new ArrayList<>();
        String selectedDate = (String) dateComboBox.getSelectedItem();

        if (selectedDate == null) {
            return new String[0];
        }

        Set<String> bookedSlots = getBookedSlots(selectedDate);

        try (Scanner scanner = new Scanner(new File("appointmentslot.txt"))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 4 && parts[0].equals(lecturer) && parts[1].equals(selectedDate)) {
                    LocalTime startTime = LocalTime.parse(parts[2]);
                    LocalTime endTime = LocalTime.parse(parts[3]);
                    while (startTime.isBefore(endTime)) {
                        LocalTime nextSlot = startTime.plusMinutes(30);
                        String slot = startTime + " - " + (nextSlot.isAfter(endTime) ? endTime : nextSlot);
                        if (!bookedSlots.contains(slot)) {
                            availableTimeSlots.add(slot);
                        }
                        startTime = nextSlot;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading the appointment slots database.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return availableTimeSlots.toArray(new String[0]);
    }

    private Set<String> getBookedDates() {
        Set<String> bookedDates = new HashSet<>();
        try (Scanner scanner = new Scanner(new File("appointment.txt"))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 5 && parts[0].equals(lecturer)) {
                    bookedDates.add(parts[1]);
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading the appointment database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return bookedDates;
    }

    private Set<String> getBookedSlots(String date) {
        Set<String> bookedSlots = new HashSet<>();
        if (date == null) {
            return bookedSlots;
        }
        try (Scanner scanner = new Scanner(new File("appointment.txt"))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 5 && parts[0].equals(lecturer) && parts[1].equals(date)) {
                    bookedSlots.add(parts[2]);
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading the appointment database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return bookedSlots;
    }

    private boolean hasAvailableSlots(String date) {
        Set<String> bookedSlots = getBookedSlots(date);
        try (Scanner scanner = new Scanner(new File("appointmentslot.txt"))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 4 && parts[0].equals(lecturer) && parts[1].equals(date)) {
                    LocalTime startTime = LocalTime.parse(parts[2]);
                    LocalTime endTime = LocalTime.parse(parts[3]);
                    while (startTime.isBefore(endTime)) {
                        LocalTime nextSlot = startTime.plusMinutes(30);
                        String slot = startTime + " - " + (nextSlot.isAfter(endTime) ? endTime : nextSlot);
                        if (!bookedSlots.contains(slot)) {
                            return true;
                        }
                        startTime = nextSlot;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading the appointment slots database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void updateTimeSlots(String username) {
        String[] updatedSlots = getAvailableTimeSlots();
        timeSlotComboBox.setModel(new DefaultComboBoxModel<>(updatedSlots));
    }

    private void confirmReschedule(String username) {
        String newDate = (String) dateComboBox.getSelectedItem();
        String newTimeSlot = (String) timeSlotComboBox.getSelectedItem();


        if (updateAppointmentInFile(lecturer, newDate, newTimeSlot, "rescheduling", username)) {
            JOptionPane.showMessageDialog(this, "Appointment rescheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            parentFrame.dispose();
            new ManageAppointmentFrame(username).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to reschedule appointment.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean updateAppointmentInFile(String lecturer, String newDate, String newTimeSlot, String newStatus, String username) {
        File inputFile = new File("appointment.txt");
        File tempFile = new File("appointment_temp.txt");

        boolean appointmentUpdated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5 && parts[0].equals(lecturer) && parts[3].equals(username)) {
                    writer.write(parts[0] + "," + newDate + "," + newTimeSlot + "," + parts[3] + "," + newStatus);
                    appointmentUpdated = true;
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating the appointment database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (appointmentUpdated) {
            if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
                JOptionPane.showMessageDialog(this, "Error renaming the temporary file.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            tempFile.delete();
            JOptionPane.showMessageDialog(this, "No matching appointment found to update.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        return true;
    }
}
