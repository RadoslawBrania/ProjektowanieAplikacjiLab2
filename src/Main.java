import java.util.*;

enum ItemCondition {
    NEW, USED, DAMAGED
}

class Vehicle implements Comparable<Vehicle> {
    private String brand;
    private String model;
    private ItemCondition condition;
    private double price;
    private int year;
    private double mileage;
    private double engineCapacity;
    private int range;

    public Vehicle(String brand, String model, ItemCondition condition, double price, int year, double mileage, double engineCapacity, int range ) {
        this.brand = brand;
        this.model = model;
        this.condition = condition;
        this.price = price;
        this.year = year;
        this.mileage = mileage;
        this.engineCapacity = engineCapacity;
        this.range = range;
    }

    public void print() {
        System.out.println("Vehicle: " + brand + " " + model +
                "\nCondition: " + condition +
                "\nPrice: " + price +
                "\nYear: " + year +
                "\nMileage: " + mileage +
                "\nEngine Capacity: " + engineCapacity);
    }
    public boolean isElectric(){
        return this.range != 0;
    }
    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public ItemCondition getCondition() {
        return condition;
    }
    public void editPrice(double price){
        this.price=price;
    }

    @Override
    public int compareTo(Vehicle other) {
        return this.brand.compareTo(other.brand);
    }
}

class CarShowroom {
    private String showroomName;
    private List<Vehicle> vehicles;
    private int maxCapacity;

    public CarShowroom(String showroomName, int maxCapacity) {
        this.showroomName = showroomName;
        this.vehicles = new ArrayList<>();
        this.maxCapacity = maxCapacity;
    }

    public void addProduct(Vehicle vehicle) {
        if (vehicles.size() >= maxCapacity) {
            System.err.println("Showroom capacity exceeded. Cannot add more vehicles.");
            return;
        }


        vehicles.add(vehicle);
    }

    public void getProduct(Vehicle vehicle) {
        vehicles.remove(vehicle);
    }

    public void removeProduct(Vehicle vehicle) {
        vehicles.removeIf(v -> vehicle.getModel().equals(v.getModel()));
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public Vehicle search(String name) {
        for (Vehicle vehicle : vehicles) {
            if (name.compareTo(vehicle.getModel())==0) {
                return vehicle;
            }
        }
        return null;
    }

    public List<Vehicle> searchPartial(String partialName) {
        List<Vehicle> matchingVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if ((vehicle.getModel().toLowerCase()).contains(partialName)) {
                System.out.println(vehicle.getModel().toLowerCase());
                matchingVehicles.add(vehicle);
            }
        }
        return matchingVehicles;
    }

    public int countByCondition(ItemCondition condition) {
        int count = 0;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getCondition() == condition) {
                count++;
            }
        }
        return count;
    }

    public void summary() {
        for (Vehicle vehicle : vehicles) {
            vehicle.print();
            System.out.println("------------");
        }
    }

    public List<Vehicle> sortByName() {
        List<Vehicle> sortedList = new ArrayList<>(vehicles);
        Collections.sort(sortedList);
        return sortedList;
    }

    public List<Vehicle> sortByAmount() {
        List<Vehicle> sortedList = new ArrayList<>(vehicles);
        sortedList.sort(Comparator.comparingInt(o -> Collections.frequency(sortedList, o)));
        Collections.reverse(sortedList);
        return sortedList;
    }

    public Vehicle max() {
        return Collections.max(vehicles, Comparator.comparingInt(o -> Collections.frequency(vehicles, o)));
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}

class CarShowroomContainer {
    private Map<String, CarShowroom> showrooms;

    public CarShowroomContainer() {
        this.showrooms = new HashMap<>();
    }

    public void addCenter(String name, int maxCapacity) {
        showrooms.put(name, new CarShowroom(name, maxCapacity));
    }

    public void removeCenter(String name) {
        showrooms.remove(name);
    }

    public List<String> findEmpty() {
        List<String> emptyShowrooms = new ArrayList<>();
        for (Map.Entry<String, CarShowroom> entry : showrooms.entrySet()) {
            if (entry.getValue().getVehicles().isEmpty()) {
                emptyShowrooms.add(entry.getKey());
            }
        }
        return emptyShowrooms;
    }

    public void summary() {
        for (Map.Entry<String, CarShowroom> entry : showrooms.entrySet()) {
            System.out.println("Showroom: " + entry.getKey() +
                    "\nPercentage filled: " + ((double) entry.getValue().getVehicles().size() / entry.getValue().getMaxCapacity()) * 100 + "%");
        }
    }

    public Map<String, CarShowroom> getShowrooms() {
        return showrooms;
    }
}

public class Main {
    public static void main(String[] args) {
        CarShowroomContainer container = new CarShowroomContainer();

        container.addCenter("Main Showroom", 10);
        container.addCenter("City Showroom", 5);

        CarShowroom mainShowroom = container.getShowrooms().get("Main Showroom");
        CarShowroom cityShowroom = container.getShowrooms().get("City Showroom");

        mainShowroom.addProduct(new Vehicle("Toyota", "Camry", ItemCondition.NEW, 30000, 2022, 0, 2.5,0));
        mainShowroom.addProduct(new Vehicle("Ford", "Fusion", ItemCondition.USED, 20000, 2019, 25000, 2.0,0));
        mainShowroom.addProduct(new Vehicle("Ford", "Fusion", ItemCondition.USED, 20000, 2019, 25000, 2.0,0));
        mainShowroom.addProduct(new Vehicle("Honda", "Accord", ItemCondition.NEW, 32000, 2023, 0, 2.0,0));

        mainShowroom.addProduct(new Vehicle("Honda", "Accord", ItemCondition.NEW, 32000, 2023, 1, 2.0,0));

        cityShowroom.addProduct(new Vehicle("Toyota", "Corolla", ItemCondition.USED, 18000, 2018, 30000, 1.8,0));
        mainShowroom.addProduct(new Vehicle("Toyota", "Corolla", ItemCondition.USED, 18000, 2018, 30000, 1.8,0));

        cityShowroom.sortByAmount();
        mainShowroom.summary();
        cityShowroom.summary();

        mainShowroom.sortByName();
        mainShowroom.removeProduct(new Vehicle("Toyota", "Camry", ItemCondition.NEW, 30000, 2022, 0, 2.5,0));
        mainShowroom.summary();

        System.out.println("Empty showrooms: " + container.findEmpty());
        System.out.println("Used condition: " + mainShowroom.countByCondition(ItemCondition.USED));
        System.out.println("Searching for Fusion " + mainShowroom.search("Fusion").getBrand());
        System.out.println("Searching for partial Co - matches count:  " + mainShowroom.searchPartial("co").size());
        System.out.println("Is the car electric? " + mainShowroom.search("Fusion").isElectric());

        container.summary();
        System.out.println(mainShowroom.max().getBrand());
    }
}