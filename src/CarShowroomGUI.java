import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.*;
import java.util.List;

public class CarShowroomGUI extends JFrame {
    private CarShowroomContainer showroomContainer;
    private JTable table;
    private DefaultListModel<String> showroomListModel;
    private DefaultListModel<Vehicle> vehicleListModel;
    private JLabel selectedShowroomLabel;
    private JComboBox<String> locationComboBox;

    private void performSearch(JTextField searchField){
            String searchKeyword = searchField.getText().trim().toLowerCase();
            List<Vehicle> searchResults = new ArrayList<>();
            for (CarShowroom showroom : showroomContainer.getShowrooms().values()) {
                if(locationComboBox.getSelectedItem()==showroom.getName() || locationComboBox.getSelectedItem()=="Any") {
                    for (Vehicle vehicle : showroom.getVehicles()) {
                        if (vehicle.getModel().toLowerCase().contains(searchKeyword) || searchKeyword=="") {
                            searchResults.add(vehicle);
                        }
                    }
                }
            }
            table.setModel(new CarShowroomTableModel(searchResults));
    }

    private void exportShowroom() throws IOException {
        String filename="Init.ser";
        File file = new File(filename);
        FileOutputStream filestr = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(filestr);

        out.writeObject(showroomContainer);
        out.close();
        filestr.close();

        // Method for serialization of object
        System.out.println(file.getAbsolutePath());
    }
    private void importShowroom() throws IOException, ClassNotFoundException {
        String filename = "Init.ser";
        try {
            FileInputStream filestr = new FileInputStream(filename);

            ObjectInputStream in = new ObjectInputStream(filestr);
            showroomContainer = (CarShowroomContainer) in.readObject();
            in.close();
            filestr.close();
            for (CarShowroom c : showroomContainer.getShowrooms().values()) {
                showroomListModel.addElement(c.getName());
                locationComboBox.addItem(c.getName());
                System.out.println(c.getName());
            }
        }
        catch(IOException ignored){

        }
    }
    private void exportSelectedShowroomToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                String selectedShowroomName = (String) locationComboBox.getSelectedItem();
                CarShowroom selectedShowroom = showroomContainer.getShowrooms().get(selectedShowroomName);
                if (selectedShowroom != null) {
                    // Pobierz listę pojazdów z wybranego salonu
                    List<Vehicle> vehicles = selectedShowroom.getVehicles();
                    // Pobierz klasę pojazdu
                    Class<?> vehicleClass = vehicles.get(0).getClass();
                    // Pobierz pola z adnotacjami CSVColumn
                    Field[] fields = vehicleClass.getDeclaredFields();
                    List<Field> annotatedFields = new ArrayList<>();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(CSVColumn.class)) {
                            annotatedFields.add(field);
                        }
                    }
                    // Utwórz nagłówki kolumn z nazwami i kolejnością z adnotacji
                    List<String> headers = new ArrayList<>();
                    for (Field field : annotatedFields) {
                        headers.add(field.getAnnotation(CSVColumn.class).value());
                    }
                    // Zapisz nagłówki do pliku
                    writer.println(String.join(",", headers));
                    // Zapisz dane pojazdów
                    for (Vehicle vehicle : vehicles) {
                        List<String> rowData = new ArrayList<>();
                        for (Field field : annotatedFields) {
                            field.setAccessible(true);
                            Object value = field.get(vehicle);
                            rowData.add(value.toString());
                        }
                        writer.println(String.join(",", rowData));
                    }
                    JOptionPane.showMessageDialog(this, "Selected showroom exported to CSV successfully!");
                }
            } catch (IOException | IllegalAccessException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting showroom to CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void importSelectedShowroomFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (Scanner scanner = new Scanner(file)) {
                // Pobierz zaznaczony salon
                showroomContainer.addCenter(file.getName(),500);
                CarShowroom selectedShowroom = showroomContainer.getShowrooms().get(file.getName());
                showroomListModel.addElement(file.getName());
                locationComboBox.addItem(file.getName());
                if (selectedShowroom != null) {
                    // Usuń obecne pojazdy ze zaznaczonego salonu
                    selectedShowroom.getVehicles().clear();
                    // Pomiń nagłówki
                    if (scanner.hasNextLine()) {
                        scanner.nextLine();
                    }
                    // Wczytaj dane z pliku i dodaj pojazdy do salonu
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] values = line.split(",");
                        if (values.length == 8) {
                            try {
                                Vehicle vehicle = getVehicle(values);
                                selectedShowroom.addProduct(vehicle);
                            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                                JOptionPane.showMessageDialog(this, "Error parsing data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid data format in CSV file.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    // Odśwież tabelę z danymi pojazdów
                    table.setModel(new CarShowroomTableModel(selectedShowroom.getVehicles()));
                    JOptionPane.showMessageDialog(this, "Selected showroom imported from CSV successfully!");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error importing showroom from CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static Vehicle getVehicle(String[] values) {
        String brand = values[0].trim();
        String model = values[1].trim();
        double price = Double.parseDouble(values[2].trim());
        int year = Integer.parseInt(values[3].trim());
        double mileage = Double.parseDouble(values[4].trim());
        double engineCapacity = Double.parseDouble(values[5].trim());
        int range = Integer.parseInt(values[6].trim());
        boolean isElectric = Boolean.parseBoolean(values[7].trim());
        Vehicle vehicle = new Vehicle(brand, model, ItemCondition.NEW, price, year, mileage, engineCapacity, range);
        vehicle.setElectric(isElectric);
        return vehicle;
    }

    public CarShowroomGUI() throws IOException, ClassNotFoundException {
        setTitle("Car Showroom Management");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        showroomContainer = new CarShowroomContainer();
        showroomListModel = new DefaultListModel<>();
        vehicleListModel = new DefaultListModel<>();
//        showroomContainer.addCenter("MainShowroom",34);
//        showroomContainer.addCenter("CityShowroom",34);
//        CarShowroom mainShowroom = showroomContainer.getShowrooms().get("MainShowroom");
//        CarShowroom cityShowroom = showroomContainer.getShowrooms().get("CityShowroom");
//
//        mainShowroom.addProduct(new Vehicle("Toyota", "Camry", ItemCondition.NEW, 30000, 2022, 0, 2.5,0));
//        mainShowroom.addProduct(new Vehicle("Ford", "Fusion", ItemCondition.USED, 20000, 2019, 25000, 2.0,0));
//        mainShowroom.addProduct(new Vehicle("Ford", "Fusion", ItemCondition.USED, 20000, 2019, 25000, 2.0,0));
//        mainShowroom.addProduct(new Vehicle("Honda", "Accord", ItemCondition.NEW, 32000, 2023, 0, 2.0,0));
//
//        mainShowroom.addProduct(new Vehicle("Honda", "Accord", ItemCondition.NEW, 32000, 2023, 1, 2.0,0));
//
//        cityShowroom.addProduct(new Vehicle("Toyota", "Corolla", ItemCondition.USED, 18000, 2018, 30000, 1.8,0));
//        mainShowroom.addProduct(new Vehicle("Toyota", "Corolla", ItemCondition.USED, 18000, 2018, 30000, 1.8,0));

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
        JPanel panel = new JPanel(new BorderLayout());
        // Panel for selected showroom label
        JPanel selectedShowroomPanel = new JPanel();
        selectedShowroomLabel = new JLabel("Selected Showroom: None");
        selectedShowroomPanel.add(selectedShowroomLabel);
        // Panel for location selection
        JLabel locationLabel = new JLabel("Select Location:");
        locationComboBox = new JComboBox<>();
        locationComboBox.addItem("Any");
        for (String location : showroomContainer.getShowrooms().keySet()) {
            locationComboBox.addItem(location);
        }
        // Panel for buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(6, 1));

        JPanel panel2 = new JPanel(new BorderLayout());
        JButton addShowroomButton = new JButton("Add Showroom");
        JButton removeShowroomButton = new JButton("Remove Showroom");
        JButton addVehicleButton = new JButton("Add Vehicle");
        JButton removeVehicleButton = new JButton("Remove Vehicle");
        JButton sortButton = new JButton("Sort Showrooms by Load");
        JButton reserveButton = new JButton("Reserve");
        JButton purchaseButton = new JButton("Purchase");
        JButton exportButton = new JButton("Export Showrooms");
        JButton importButton = new JButton("Import Showrooms");
        JButton importCsvButton = new JButton("Import Showroom from CSV");
        JButton exportCsvButton = new JButton("Export Showroom to CSV");

        buttonsPanel.add(importCsvButton);
        buttonsPanel.add(exportCsvButton);
        buttonsPanel.add(exportButton);
        buttonsPanel.add(importButton);
        buttonsPanel.add(locationComboBox);
        buttonsPanel.add(addShowroomButton);
        buttonsPanel.add(removeShowroomButton);
        buttonsPanel.add(addVehicleButton);
        buttonsPanel.add(removeVehicleButton);
        buttonsPanel.add(sortButton);
        buttonsPanel.add(reserveButton);
        buttonsPanel.add(purchaseButton);



        panel.add(showroomsPanel, BorderLayout.WEST);
        panel.add(vehiclesPanel, BorderLayout.CENTER);
        panel.add(selectedShowroomPanel, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.EAST);
        add(panel);
        // Panel for search
        JPanel searchPanel = new JPanel(new BorderLayout());
        JLabel searchLabel = new JLabel("Search by Name:");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        panel.add(searchPanel, BorderLayout.NORTH);


        importCsvButton.addActionListener(e->{
            importSelectedShowroomFromCSV();
        });


        exportCsvButton.addActionListener(e->{
            exportSelectedShowroomToCSV();
        });
        // Add action listener for search button
        searchButton.addActionListener(e -> {
            performSearch(searchField);
        });
        // Add action listeners for reserve and purchase buttons
        reserveButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                Vehicle selectedVehicle = ((CarShowroomTableModel) table.getModel()).getVehicleAt(selectedRow);
                if (selectedVehicle != null) {
                    selectedVehicle.isReserved=true;
                }
            }
        });
        exportButton.addActionListener(e -> {
            try {
                exportShowroom();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        importButton.addActionListener(e->{
            try{
                importShowroom();
            } catch(IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
            }
        });
        purchaseButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                Vehicle selectedVehicle = ((CarShowroomTableModel) table.getModel()).getVehicleAt(selectedRow);
                if (selectedVehicle != null && !selectedVehicle.isReserved) {
                    String selectedShowroomName = selectedShowroomLabel.getText().substring("Selected Showroom: ".length());
                    CarShowroom selectedShowroom = showroomContainer.getShowrooms().get(selectedShowroomName);
                    if (selectedShowroom != null) {
                        selectedShowroom.removeProduct(selectedVehicle); // Remove vehicle from showroom
                        performSearch(searchField);
                    }
                }
            }
        });
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                JTable table = (JTable) e.getSource();
                Point point = e.getPoint();
                int row = table.rowAtPoint(point);
                if (row >= 0) {
                    Vehicle vehicle = ((CarShowroomTableModel) table.getModel()).getVehicleAt(row);
                    if (vehicle != null) {
                        String showroomName = null;
                        for (Map.Entry<String, CarShowroom> entry : showroomContainer.getShowrooms().entrySet()) {
                            if (entry.getValue().getVehicles().contains(vehicle)) {
                                showroomName = entry.getKey();
                                break;
                            }
                        }
                        table.setToolTipText("Brand: " + vehicle.getBrand() +
                                "\nModel: " + vehicle.getModel() +
                                "\nCondition: " + vehicle.getCondition() +
                                "\nPrice: " + vehicle.getPrice() +
                                "\nYear: " + vehicle.getYear() +
                                "\nMileage: " + vehicle.getMileage() +
                                "\nEngine Capacity: " + vehicle.getEngineCapacity() +
                                "\nIs Reserved?: " + vehicle.isReserved +
                                "\nShowroom: " + showroomName);
                    }
                }
            }
        });

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
            locationComboBox.addItem(showroomName);
        });

        removeShowroomButton.addActionListener(e -> {
            String selectedShowroom = showroomsList.getSelectedValue();
            if (selectedShowroom != null) {
                showroomContainer.removeCenter(selectedShowroom);
                showroomListModel.removeElement(selectedShowroom);
                selectedShowroomLabel.setText("Selected Showroom: None");
                table.setModel(new DefaultTableModel()); // Clear table
            }
            locationComboBox.removeItem(selectedShowroom);
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
        locationComboBox.addActionListener(e -> {
            String selectedLocation = (String) locationComboBox.getSelectedItem();
            if (selectedLocation != null && selectedLocation.equals("Any")) {
                List<Vehicle> allVehicles = new ArrayList<>();
                for (CarShowroom showroom : showroomContainer.getShowrooms().values()) {
                    allVehicles.addAll(showroom.getVehicles());
                }
                Collections.sort(allVehicles,(vehicle1,vehicle2) ->{
                    int priceComparison = Double.compare(vehicle1.getPrice(), vehicle2.getPrice());
                    if (priceComparison != 0) {
                        return priceComparison;
                    }
                    int nameComparison = vehicle1.getModel().compareTo(vehicle2.getModel());
                    if (nameComparison != 0) {
                        return nameComparison;
                    }
                    return Integer.compare(vehicle1.getYear(), vehicle2.getYear());
                });
                table.setModel(new CarShowroomTableModel(allVehicles));
            } else if (selectedLocation != null) {
                CarShowroom selectedShowroom = showroomContainer.getShowrooms().get(selectedLocation);
                if (selectedShowroom != null) {
                    table.setModel(new CarShowroomTableModel(selectedShowroom.getVehicles()));
                }
            }
        });
        importShowroom();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CarShowroomGUI showroomGUI = null;
            try {
                showroomGUI = new CarShowroomGUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
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
    public Vehicle getVehicleAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < data.size()) {
            return data.get(rowIndex);
        }
        return null;
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
