import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class ManageConsultationSlotFrame extends JFrame {
    private DefaultTableModel tableModel;

    public ManageConsultationSlotFrame(String username) {
        setTitle("Manage Consultation Slots");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainContentContainer = new JPanel(new BorderLayout());
        mainContentContainer.setOpaque(false);
        mainContentContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sidebarPanel = createSidebarPanel(username);
        JScrollPane tableScrollPane = createSlotsTable(username);
        JPanel whiteFramePanel = createWhiteFramePanel("Manage Your Consultation Slots", tableScrollPane, username);

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

        String[] options = {"Manage Consultation Slot", "Manage Consultation","Consultation Feedback", "Logout"};

        for (String option : options) {
            JButton button = createSidebarButton(option);
            if ("Manage Consultation Slot".equals(option)) {
                button.setBackground(new Color(39, 174, 96)); // Highlight current page
            }

            button.addActionListener(_ -> {
                switch (option) {
                    case "Manage Consultation":
                        dispose();
                        new ManageConsultationFrame(username).setVisible(true);
                        break;
                    case "Consultation Feedback":
                        dispose();
                        new ConsultationFeedbackFrame(username).setVisible(true);// var
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

    private JScrollPane createSlotsTable(String username) {
        String[] columnNames = {"Date", "Start Time", "End Time", "Actions"};
        tableModel = new DefaultTableModel(new Object[0][0], columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only "Actions" column is editable
            }
        };
    
        JTable slotsTable = new JTable(tableModel);
        slotsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        slotsTable.setRowHeight(40);
        slotsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    
        // Set custom renderer and editor for "Actions" column
        slotsTable.getColumnModel().getColumn(3).setCellRenderer(new EditRemoveButtonRenderer());
        slotsTable.getColumnModel().getColumn(3).setCellEditor(new EditRemoveButtonEditor(new JCheckBox(), slotsTable, this, username));
    
        loadSlotsFromFile(username);
    
        return new JScrollPane(slotsTable);
    }
    
    private JPanel createWhiteFramePanel(String title, JScrollPane tableScrollPane, String username) {
        JPanel whiteFramePanel = new JPanel(new BorderLayout());
        whiteFramePanel.setBackground(new Color(255, 255, 255, 200));
        whiteFramePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        whiteFramePanel.setOpaque(true);
    
        // Create a container for the title and the button
        JPanel titlePanel = new JPanel(new GridBagLayout());
        titlePanel.setOpaque(false);
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Allow the title to expand and center
    
        // Title label
        JLabel titleLabel = new JLabel(title, JLabel.CENTER); // Center the title
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(JLabel.CENTER); // Explicit center alignment
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.anchor = GridBagConstraints.CENTER; // Center alignment
        gbc.weightx = 1.0; // Ensure the label takes space to center
        titlePanel.add(titleLabel, gbc);
    
        // "Add New Slot" button
        JButton addNewSlotButton = new JButton("Add New Slot");
        addNewSlotButton.setFont(new Font("Arial", Font.BOLD, 14));
        addNewSlotButton.setBackground(new Color(41, 128, 185)); // Blue color
        addNewSlotButton.setForeground(Color.WHITE);
        addNewSlotButton.setFocusPainted(false);
        addNewSlotButton.setBorderPainted(false);
        addNewSlotButton.setOpaque(true);
        addNewSlotButton.setPreferredSize(new Dimension(150, 40)); // Set button size
        addNewSlotButton.addActionListener(_ -> {
            AddSlotDialog dialog = new AddSlotDialog(this);
            dialog.setVisible(true);
        
            if (dialog.isConfirmed()) {
                String date = dialog.getDate();
                String startTime = dialog.getStartTime();
                String endTime = dialog.getEndTime();
        
                if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Add row to table
                    tableModel.addRow(new Object[]{date, startTime, endTime, "Actions"});
        
                    // Save to file (appointmentslot.txt)
                    saveNewSlotToFile(username, date, startTime, endTime);
        
                    JOptionPane.showMessageDialog(this, "New slot added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });       
            
        gbc.gridx = 1; // Column 1
        gbc.gridy = 0; // Row 0
        gbc.anchor = GridBagConstraints.EAST; // Align button to the right
        gbc.weightx = 0; // Ensure the button does not stretch
        titlePanel.add(addNewSlotButton, gbc);
    
        // Add title panel to the top and table scroll pane to the center
        whiteFramePanel.add(titlePanel, BorderLayout.NORTH);
        whiteFramePanel.add(tableScrollPane, BorderLayout.CENTER);
    
        return whiteFramePanel;
    }
    
    // Method to save the new slot to appointmentslot.txt
    private void saveNewSlotToFile(String username, String date, String startTime, String endTime) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("appointmentslot.txt", true))) {
            writer.write(username + "," + date + "," + startTime + "," + endTime);
            writer.newLine(); // Add a new line after each entry
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving new slot to file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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

    private void loadSlotsFromFile(String username) {
        tableModel.setRowCount(0); // Clear the table
        try (Scanner scanner = new Scanner(new File("appointmentslot.txt"))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 4 && parts[0].equals(username)) {
                    tableModel.addRow(new Object[]{parts[1], parts[2], parts[3], "Actions"});
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading appointment slots file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    boolean updateSlotInFile(String username, String oldDate, String oldStartTime, String newDate, String newStartTime, String newEndTime) {
        File inputFile = new File("appointmentslot.txt");
        File tempFile = new File("appointmentslot_temp.txt");
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4 && parts[0].equals(username) &&
                        parts[1].equals(oldDate) && parts[2].equals(oldStartTime)) {
                    writer.write(username + "," + newDate + "," + newStartTime + "," + newEndTime);
                    updated = true;
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating appointment slots file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (updated) {
            inputFile.delete();
            tempFile.renameTo(inputFile);
        } else {
            tempFile.delete();
        }

        return updated;
    }

    boolean removeSlotFromFile(String username, String date, String startTime) {
        File inputFile = new File("appointmentslot.txt");
        File tempFile = new File("appointmentslot_temp.txt");
        boolean removed = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4 && parts[0].equals(username) &&
                        parts[1].equals(date) && parts[2].equals(startTime)) {
                    removed = true;
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error removing appointment slot: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (removed) {
            inputFile.delete();
            tempFile.renameTo(inputFile);
        } else {
            tempFile.delete();
        }

        return removed;
    }
}

// Slot Action Button Renderer
class SlotActionButtonRenderer extends JPanel implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(createButton("Edit"));
        panel.add(createButton("Remove"));
        return panel;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        return button;
    }
}

// Slot Action Button Editor
class ButtonEditor extends DefaultCellEditor {
    private final JButton button;
    private final Object parentFrame;

    public ButtonEditor(JCheckBox checkBox, JTable table, BookAppointmentFrame parentFrame) {
        super(checkBox);
        this.parentFrame = parentFrame;
        button = createButton();
        button.addActionListener(_ -> handleAction(table, table.getSelectedRow()));
    }

    public ButtonEditor(JCheckBox checkBox, JTable table, ManageConsultationSlotFrame parentFrame) {
        super(checkBox);
        this.parentFrame = parentFrame;
        button = createButton();
        button.addActionListener(_ -> handleAction(table, table.getSelectedRow()));
    }

    private JButton createButton() {
        JButton btn = new JButton("Edit");
        btn.setOpaque(true);
        btn.setBackground(new Color(46, 204, 113));
        btn.setForeground(Color.WHITE);
        return btn;
    }

    private void handleAction(JTable table, int row) {
        if (parentFrame instanceof BookAppointmentFrame) {
            ((BookAppointmentFrame) parentFrame).bookAppointment(table, row);
        } else if (parentFrame instanceof ManageConsultationSlotFrame) {
            // Handle ManageAvaliableSlotFrame-specific actions
        }
    }
}

class EditRemoveButtonRenderer extends JPanel implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // Create "Edit" button
        JButton editButton = createStyledButton("Edit", new Color(41, 128, 185)); // Blue color
        
        // Create "Remove" button
        JButton removeButton = createStyledButton("Remove", new Color(231, 76, 60)); // Red color
        
        panel.add(editButton);
        panel.add(removeButton);
        return panel;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setOpaque(true);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        return button;
    }
}

class EditRemoveButtonEditor extends DefaultCellEditor {
    private JPanel panel;
    private JTable table;
    private ManageConsultationSlotFrame parentFrame;
    private String username;

    public EditRemoveButtonEditor(JCheckBox checkBox, JTable table, ManageConsultationSlotFrame parentFrame, String username) {
        super(checkBox);
        this.table = table;
        this.parentFrame = parentFrame;
        this.username = username;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // "Edit" button
        JButton editButton = createStyledButton("Edit", new Color(41, 128, 185));
        editButton.addActionListener(_ -> handleEditAction(row));
        
        // "Remove" button
        JButton removeButton = createStyledButton("Remove", new Color(231, 76, 60));
        removeButton.addActionListener(_ -> handleRemoveAction(row));

        panel.add(editButton);
        panel.add(removeButton);
        return panel;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setOpaque(true);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        return button;
    }

    private void handleEditAction(int row) {
        String currentDate = (String) table.getValueAt(row, 0);
        String currentStartTime = (String) table.getValueAt(row, 1);
        String currentEndTime = (String) table.getValueAt(row, 2);
    
        // Show the EditSlotDialog
        EditSlotDialog dialog = new EditSlotDialog(parentFrame, currentDate, currentStartTime, currentEndTime);
        dialog.setVisible(true);
    
        // If the user confirms the changes, update the slot
        if (dialog.isConfirmed()) {
            String newDate = dialog.getDate();
            String newStartTime = dialog.getStartTime();
            String newEndTime = dialog.getEndTime();
    
            if (parentFrame.updateSlotInFile(username, currentDate, currentStartTime, newDate, newStartTime, newEndTime)) {
                table.setValueAt(newDate, row, 0);
                table.setValueAt(newStartTime, row, 1);
                table.setValueAt(newEndTime, row, 2);
                JOptionPane.showMessageDialog(parentFrame, "Slot updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Failed to update slot.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleRemoveAction(int row) {
        String date = (String) table.getValueAt(row, 0);
        String startTime = (String) table.getValueAt(row, 1);

        if (parentFrame.removeSlotFromFile(username, date, startTime)) {
            ((DefaultTableModel) table.getModel()).removeRow(row);
            JOptionPane.showMessageDialog(parentFrame, "Slot removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(parentFrame, "Failed to remove slot.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class EditSlotDialog extends JDialog {
    private JTextField dateField;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private boolean confirmed;

    public EditSlotDialog(JFrame parent, String currentDate, String currentStartTime, String currentEndTime) {
        super(parent, "Edit Appointment Slot", true);
        setLayout(new GridLayout(4, 2, 10, 10));
        setSize(400, 250);
        setLocationRelativeTo(parent);

        // Date Field
        add(new JLabel("Date:"));
        dateField = new JTextField(currentDate);
        add(dateField);

        // Start Time Field
        add(new JLabel("Start Time:"));
        startTimeField = new JTextField(currentStartTime);
        add(startTimeField);

        // End Time Field
        add(new JLabel("End Time:"));
        endTimeField = new JTextField(currentEndTime);
        add(endTimeField);

        // Styled Buttons
        JButton confirmButton = createStyledButton("Confirm", new Color(41, 128, 185)); // Blue color
        JButton cancelButton = createStyledButton("Cancel", new Color(231, 76, 60));   // Red color

        confirmButton.addActionListener(_ -> {
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(_ -> dispose());

        add(confirmButton);
        add(cancelButton);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setOpaque(true);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        return button;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getDate() {
        return dateField.getText();
    }

    public String getStartTime() {
        return startTimeField.getText();
    }

    public String getEndTime() {
        return endTimeField.getText();
    }
}

class AddSlotDialog extends JDialog {
    private JTextField dateField;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private boolean confirmed;

    public AddSlotDialog(JFrame parent) {
        super(parent, "Add New Appointment Slot", true);
        setLayout(new GridLayout(4, 2, 10, 10));
        setSize(400, 250);
        setLocationRelativeTo(parent);

        // Date Field
        add(new JLabel("Date:"));
        dateField = new JTextField(); // Empty field for input
        add(dateField);

        // Start Time Field
        add(new JLabel("Start Time:"));
        startTimeField = new JTextField(); // Empty field for input
        add(startTimeField);

        // End Time Field
        add(new JLabel("End Time:"));
        endTimeField = new JTextField(); // Empty field for input
        add(endTimeField);

        // Styled Buttons
        JButton confirmButton = createStyledButton("Confirm", new Color(41, 128, 185)); // Blue color
        JButton cancelButton = createStyledButton("Cancel", new Color(231, 76, 60));   // Red color

        confirmButton.addActionListener(_ -> {
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(_ -> dispose());

        add(confirmButton);
        add(cancelButton);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setOpaque(true);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        return button;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getDate() {
        return dateField.getText();
    }

    public String getStartTime() {
        return startTimeField.getText();
    }

    public String getEndTime() {
        return endTimeField.getText();
    }
}
