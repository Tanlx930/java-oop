import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class BookAppointmentFrame extends JFrame {
    private final String username;

    public BookAppointmentFrame(String username) {
        this.username = username;
        setTitle("Homepage - Welcome " + username);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
    
        JPanel sidebarPanel = createSidebarPanel(username);
        JScrollPane tableScrollPane = createLecturersTableFromFile("appointmentslot.txt");
        JPanel whiteFramePanel = createWhiteFramePanel("Available Appointment Slots", tableScrollPane);
    
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
    
        String[] options= new String[]{"Book Appointment", "Manage Appointment", "Appointment Feedback", "Logout"};
        
    
        for (String option : options) {
            JButton button = createSidebarButton(option);
            if ("Book Appointment".equals(option)) {
                button.setBackground(new Color(39, 174, 96)); // Highlight the current page
            }
            button.addActionListener(_ -> {
                switch (option) {
                    case "Manage Appointment":
                        dispose();
                        new ManageAppointmentFrame(username).setVisible(true);
                        break;
                    case "Appointment Feedback":
                        dispose();
                        new AppointmentFeedbackFrame(username).setVisible(true);
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
    
    private JScrollPane createLecturersTableFromFile(String filePath) {
        String[] columnNames = {"Username", "Date", "Actions"};
        Map<String, Map<String, String>> groupedData = new HashMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            if (scanner.hasNextLine()) scanner.nextLine();
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 4) {
                    String username = parts[0];
                    String date = parts[1];
                    LocalDate availableDate = LocalDate.parse(date, dateFormatter);
                    if (!availableDate.isBefore(today)) {
                        String timeSlot = parts[2] + " - " + parts[3];
                        groupedData.computeIfAbsent(username, _ -> new HashMap<>())
                            .put(date, timeSlot);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading the appointment slots database.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        List<Object[]> finalData = new ArrayList<>();
        groupedData.forEach((username, dateMap) -> dateMap.forEach((date, timeSlot) -> {
            finalData.add(new Object[]{username, date, "Book"});
        }));

        // Sort the finalData list by date first and then by username
        finalData.sort(Comparator.comparing((Object[] arr) -> LocalDate.parse((String) arr[1], dateFormatter)).thenComparing(arr -> (String) arr[0]));

        DefaultTableModel tableModel = new DefaultTableModel(finalData.toArray(new Object[0][]), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };

        JTable lecturersTable = new JTable(tableModel);
        lecturersTable.setFont(new Font("Arial", Font.PLAIN, 14));
        lecturersTable.setRowHeight(lecturersTable.getRowHeight() * 2);
        lecturersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        lecturersTable.getTableHeader().setReorderingAllowed(false);

        lecturersTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        lecturersTable.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox(), lecturersTable, this));

        return new JScrollPane(lecturersTable);
    }

    private JPanel createWhiteFramePanel(String title, JScrollPane tableScrollPane) {
        JPanel whiteFramePanel = new JPanel(new BorderLayout());
        whiteFramePanel.setBackground(new Color(255, 255, 255, 200));
        whiteFramePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        whiteFramePanel.setOpaque(true);
        whiteFramePanel.add(new JLabel(title, JLabel.CENTER) {{
            setFont(new Font("Arial", Font.BOLD, 24));
            setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        }}, BorderLayout.NORTH);
        whiteFramePanel.add(tableScrollPane, BorderLayout.CENTER);
        return whiteFramePanel;
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

    public void bookAppointment(JTable table, int selectedRow) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "No lecturer selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String lecturerName = (String) table.getValueAt(selectedRow, 0);
        String date = (String) table.getValueAt(selectedRow, 1);
        String availableSlots = "10:00 - 16:00"; // Simplified for demonstration
    
        List<String> slots = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String[] times = availableSlots.split(" - ");
        LocalTime startTime = LocalTime.parse(times[0], formatter);
        LocalTime endTime = LocalTime.parse(times[1], formatter);
        while (!startTime.isAfter(endTime.minusMinutes(30))) {
            slots.add(startTime.format(formatter) + " - " + startTime.plusMinutes(30).format(formatter));
            startTime = startTime.plusMinutes(30);
        }
    
        // Remove already booked slots
        List<String> bookedSlots = getBookedSlots();
        slots.removeAll(bookedSlots);
    
        String selectedSlot = (String) JOptionPane.showInputDialog(this, "Available slots for " + lecturerName + " on " + date + ":", "Select Slot", JOptionPane.PLAIN_MESSAGE, null, slots.toArray(), slots.isEmpty() ? null : slots.get(0));
    
        if (selectedSlot != null && !selectedSlot.trim().isEmpty()) {
            String status="booked";
            String appointmentDetails = lecturerName + "," + date + "," + selectedSlot + "," + username + "," + status;
    
            try (FileWriter writer = new FileWriter("appointment.txt", true)) {
                writer.write(appointmentDetails + "\n");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving the appointment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
    
            JOptionPane.showMessageDialog(this, "Appointment booked with " + lecturerName + " at " + date + " " + selectedSlot + ".", "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Booking canceled.", "Booking Canceled", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private List<String> getBookedSlots() {
        List<String> bookedSlots = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File("appointment.txt"))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 4) {
                    bookedSlots.add(parts[2]);
                }
            }
        } catch (FileNotFoundException e) {
            // No appointments booked yet
        }
        return bookedSlots;
    }
}


class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
        setText("Book");
        setBackground(new Color(46, 204, 113));
        setForeground(Color.WHITE);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}

class ButtonEditor extends DefaultCellEditor {
    private final JButton button;
    private final BookAppointmentFrame  parentFrame;

    public ButtonEditor(JCheckBox checkBox, JTable table, BookAppointmentFrame  parentFrame) {
        super(checkBox);
        this.parentFrame = parentFrame;
        button = createButton();
        button.addActionListener(_ -> handleAction(table, button));
    }

    private JButton createButton() {
        JButton btn = new JButton("Book");
        btn.setOpaque(true);
        btn.setBackground(new Color(46, 204, 113));
        btn.setForeground(Color.WHITE);
        return btn;
    }

    private void handleAction(JTable table, JButton button) {
        int row = table.getEditingRow();  // Get the currently editing row
        if (row != -1 && parentFrame instanceof BookAppointmentFrame) {
            ((BookAppointmentFrame) parentFrame).bookAppointment(table, row);
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return button;
    }
}

class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getBackground());
        g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
        super.paint(g2, c);
    }
}
