import java.io.Serializable;
import java.util.*;

enum ItemCondition {
    NEW, USED, DAMAGED
}

class Vehicle implements Comparable<Vehicle>, Serializable {
    @CSVColumn("Brand")
    private String brand;

    @CSVColumn("Model")
    private String model;

    @CSVColumn("Price")
    private double price;

    @CSVColumn("Year")
    private int year;

    @CSVColumn("Mileage")
    private double mileage;

    @CSVColumn("Engine Capacity")
    private double engineCapacity;
    @CSVColumn("Range")
    private int range;

    @CSVColumn("Is Electric")
    private boolean isElectric;

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMileage(double mileage) {
        this.mileage = mileage;
    }

    public void setEngineCapacity(double engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setElectric(boolean electric) {
        isElectric = electric;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }

    private ItemCondition condition;

    public boolean isReserved=false;
    public Vehicle(){
        this.brand = "";
        this.model = "";
        this.condition = ItemCondition.NEW;
        this.price = 0;
        this.year = 0;
        this.mileage = 0;
        this.engineCapacity = 0;
        this.range = 0;
    }

    public Vehicle(String brand, String model, ItemCondition condition, double price, int year, double mileage, double engineCapacity, int range ) {
        this.brand = brand;
        this.model = model;
        this.condition = condition;
        this.price = price;
        this.year = year;
        this.mileage = mileage;
        this.engineCapacity = engineCapacity;
        this.range = range;
        if(this.condition==null){
            this.condition=ItemCondition.NEW;
        }
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

    public double getPrice() {
        return price;
    }

    public int getYear() {
    return year;
    }
    public double getMileage(){
        return mileage;
    }

    public double getEngineCapacity() {
    return engineCapacity;
    }
}

class CarShowroom implements Serializable{
    private String showroomName;
    private java.util.List<Vehicle> vehicles;
    private int maxCapacity;

    public CarShowroom(String showroomName, int maxCapacity) {
        this.showroomName = showroomName;
        this.vehicles = new ArrayList<>();
        this.maxCapacity = maxCapacity;
    }
    public String getName(){ return showroomName; }

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

    public java.util.List<Vehicle> getVehicles() {
        vehicles.sort((vehicle1,vehicle2) ->{
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

    public java.util.List<Vehicle> searchPartial(String partialName) {
        java.util.List<Vehicle> matchingVehicles = new ArrayList<>();
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

    public java.util.List<Vehicle> sortByName() {
        java.util.List<Vehicle> sortedList = new ArrayList<>(vehicles);
        Collections.sort(sortedList);
        return sortedList;
    }

    public java.util.List<Vehicle> sortByAmount() {
        java.util.List<Vehicle> sortedList = new ArrayList<>(vehicles);
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

class CarShowroomContainer implements Serializable {
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

    public java.util.List<String> findEmpty() {
        java.util.List<String> emptyShowrooms = new ArrayList<>();
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

}

//public class Main {
//    public static void main(String[] args) {
//        CarShowroomContainer container = new CarShowroomContainer();
//
//        container.addCenter("Main Showroom", 10);
//        container.addCenter("City Showroom", 5);
//
//        CarShowroom mainShowroom = container.getShowrooms().get("Main Showroom");
//        CarShowroom cityShowroom = container.getShowrooms().get("City Showroom");
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
//
//        cityShowroom.sortByAmount();
//        mainShowroom.summary();
//        cityShowroom.summary();
//
//        mainShowroom.sortByName();
//        mainShowroom.removeProduct(new Vehicle("Toyota", "Camry", ItemCondition.NEW, 30000, 2022, 0, 2.5,0));
//        mainShowroom.summary();
//
//        System.out.println("Empty showrooms: " + container.findEmpty());
//        System.out.println("Used condition: " + mainShowroom.countByCondition(ItemCondition.USED));
//        System.out.println("Searching for Fusion " + mainShowroom.search("Fusion").getBrand());
//        System.out.println("Searching for partial Co - matches count:  " + mainShowroom.searchPartial("co").size());
//        System.out.println("Is the car electric? " + mainShowroom.search("Fusion").isElectric());
//
//        container.summary();
//        System.out.println(mainShowroom.max().getBrand());
//    }
