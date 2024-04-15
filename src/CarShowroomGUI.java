import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.*;
import java.util.Comparator;
import java.util.List;

public class CarShowroomGUI extends JFrame {
    private CarShowroomContainer showroomContainer;
    private JTable table;
    private DefaultListModel<String> showroomListModel;
    private DefaultListModel<Vehicle> vehicleListModel;
    private JLabel selectedShowroomLabel;

    public CarShowroomGUI() {
        setTitle("Car Showroom Management");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        showroomContainer = new CarShowroomContainer();
        showroomListModel = new DefaultListModel<>();
        vehicleListModel = new DefaultListModel<>();

        JPanel panel = new JPanel(new BorderLayout());

        // Panel for showrooms list
        JPanel showroomsPanel = new JPanel(new BorderLayout());
        showroomListModel.addAll(showroomContainer.getShowrooms().keySet());
        JList<String> showroomsList = new JList<>(showroomListModel);
        JScrollPane showroomsScrollPane = new JScrollPane(showroomsList);
        showroomsPanel.add(new JLabel("Car Showrooms"), BorderLayout.NORTH);
        showroomsPanel.add(showroomsScrollPane, BorderLayout.CENTER);

        // Panel for vehicles table
        JPanel vehiclesPanel = new JPanel(new BorderLayout());
        table = new JTable();
        JScrollPane vehiclesScrollPane = new JScrollPane(table);
        vehiclesPanel.add(new JLabel("Vehicles"), BorderLayout.NORTH);
        vehiclesPanel.add(vehiclesScrollPane, BorderLayout.CENTER);

        // Panel for selected showroom label
        JPanel selectedShowroomPanel = new JPanel();
        selectedShowroomLabel = new JLabel("Selected Showroom: None");
        selectedShowroomPanel.add(selectedShowroomLabel);

        // Panel for buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(5, 1));
        JButton addShowroomButton = new JButton("Add Showroom");
        JButton removeShowroomButton = new JButton("Remove Showroom");
        JButton addVehicleButton = new JButton("Add Vehicle");
        JButton removeVehicleButton = new JButton("Remove Vehicle");
        JButton sortButton = new JButton("Sort Showrooms by Load");
        buttonsPanel.add(addShowroomButton);
        buttonsPanel.add(removeShowroomButton);
        buttonsPanel.add(addVehicleButton);
        buttonsPanel.add(removeVehicleButton);
        buttonsPanel.add(sortButton);

        panel.add(showroomsPanel, BorderLayout.WEST);
        panel.add(vehiclesPanel, BorderLayout.CENTER);
        panel.add(selectedShowroomPanel, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.EAST);
        add(panel);

        showroomsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedShowroom = showroomsList.getSelectedValue();
                if (selectedShowroom != null) {
                    selectedShowroomLabel.setText("Selected Showroom: " + selectedShowroom);
                    CarShowroom showroom = showroomContainer.getShowrooms().get(selectedShowroom);
                    if (showroom != null) {
                        table.setModel(new CarShowroomTableModel(showroom.getVehicles()));
                    }
                }
            }
        });

        addShowroomButton.addActionListener(e -> {
            String showroomName = JOptionPane.showInputDialog(this, "Enter Showroom Name:");
            if (showroomName != null && !showroomName.isEmpty()) {
                showroomContainer.addCenter(showroomName, 10); // Assuming max capacity as 10 for demonstration
                showroomListModel.addElement(showroomName);
            }
        });

        removeShowroomButton.addActionListener(e -> {
            String selectedShowroom = showroomsList.getSelectedValue();
            if (selectedShowroom != null) {
                showroomContainer.removeCenter(selectedShowroom);
                showroomListModel.removeElement(selectedShowroom);
                selectedShowroomLabel.setText("Selected Showroom: None");
                table.setModel(new DefaultTableModel()); // Clear table
            }
        });

        addVehicleButton.addActionListener(e -> {
            String selectedShowroom = showroomsList.getSelectedValue();
            if (selectedShowroom != null) {
                CarShowroom showroom = showroomContainer.getShowrooms().get(selectedShowroom);
                if (showroom != null) {
                    String brand = JOptionPane.showInputDialog(this, "Enter Vehicle Brand:");
                    String model = JOptionPane.showInputDialog(this, "Enter Vehicle Model:");
                    double price = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter Price:"));
                    double mileage = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter mileage:"));
                    int year = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Year:"));
                    double capacity  = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter engineCapacity:"));
                    int range = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter range:"));
                    showroom.addProduct(new Vehicle(brand, model, ItemCondition.NEW, price, year, mileage, capacity, range)); // Dummy data for demonstration
                    table.setModel(new CarShowroomTableModel(showroom.getVehicles()));
                }
            }
        });

        removeVehicleButton.addActionListener(e -> {
            CarShowroom selectedShowroom = showroomContainer.getShowrooms().get(showroomsList.getSelectedValue());
            if (selectedShowroom != null) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    selectedShowroom.removeProduct(selectedShowroom.getVehicles().get(selectedRow));
                    table.setModel(new CarShowroomTableModel(selectedShowroom.getVehicles()));
                }
            }
        });


        sortButton.addActionListener(e -> {
            List<String> showroomNames = new ArrayList<>(showroomContainer.getShowrooms().keySet());
            Collections.sort(showroomNames, (s1, s2) -> {
                int carsInShowroom1 = showroomContainer.getShowrooms().get(s1).getVehicles().size();
                int carsInShowroom2 = showroomContainer.getShowrooms().get(s2).getVehicles().size();
                return Integer.compare(carsInShowroom2, carsInShowroom1);
            });

            showroomListModel.clear();
            showroomListModel.addAll(showroomNames);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CarShowroomGUI showroomGUI = new CarShowroomGUI();
            showroomGUI.setVisible(true);
        });
    }
}

class CarShowroomTableModel extends AbstractTableModel {
    private String[] columnNames = {"Brand", "Model", "Condition", "Price", "Year", "Mileage", "Engine Capacity", "Is electric?"};
    private List<Vehicle> data;

    public CarShowroomTableModel(List<Vehicle> data) {
        this.data = data;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Vehicle vehicle = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return vehicle.getBrand();
            case 1:
                return vehicle.getModel();
            case 2:
                return vehicle.getCondition();
            case 3:
                return vehicle.getPrice();
            case 4:
                return vehicle.getYear();
            case 5:
                return vehicle.getMileage();
            case 6:
                return vehicle.getEngineCapacity();
            case 7:
                return vehicle.isElectric() ? "Electric" : "Non-electric";
            default:
                return null;
        }
    }
}
