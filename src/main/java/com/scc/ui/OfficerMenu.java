package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.VehicleImpoundDao;
import com.scc.model.Impoundment;
import com.scc.model.User;
import com.scc.model.Vehicle;
import com.scc.model.VehicleOwner;

import java.time.LocalDate;
import java.util.Scanner;

public class OfficerMenu {
    private final User currentUser;
    private final VehicleImpoundDao impoundDao = new VehicleImpoundDao();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner scanner = new Scanner(System.in);

    public OfficerMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n--- IMPOUND OFFICER MENU ---");
            System.out.println("1. Add Newly Impounded Vehicle");
            System.out.println("2. View All Vehicle Records");
            System.out.println("3. Search Vehicle Records");
            System.out.println("4. Update Vehicle Information");
            System.out.println("5. Track / Update Vehicle Status");
            System.out.println("0. Logout");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addVehicleRecord();
                case "2" -> impoundDao.getAllRecords().forEach(System.out::println);
                case "3" -> searchRecords();
                case "4" -> updateVehicle();
                case "5" -> updateStatus();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid selection.");
            }
        }
    }

    private void addVehicleRecord() {
        System.out.println("\n--- Register Impounded Vehicle ---");
        System.out.print("Owner Name: ");
        String ownerName = scanner.nextLine().trim();
        System.out.print("Contact Number: ");
        String contact = scanner.nextLine().trim();
        System.out.print("Address: ");
        String address = scanner.nextLine().trim();
        System.out.print("Email (optional): ");
        String email = scanner.nextLine().trim();

        System.out.print("Plate Number: ");
        String plate = scanner.nextLine().trim();
        System.out.print("Vehicle Type (Car/Motorcycle/Truck): ");
        String type = scanner.nextLine().trim();
        System.out.print("Make: ");
        String make = scanner.nextLine().trim();
        System.out.print("Model: ");
        String model = scanner.nextLine().trim();
        System.out.print("Color: ");
        String color = scanner.nextLine().trim();

        System.out.print("Impound Location: ");
        String location = scanner.nextLine().trim();
        System.out.print("Reason for Impoundment: ");
        String reason = scanner.nextLine().trim();

        VehicleOwner owner = new VehicleOwner(0, ownerName, contact, address, email);
        Vehicle vehicle = new Vehicle(0, 0, plate, type, make, model, color);
        Impoundment impoundment = new Impoundment(0, 0, LocalDate.now().toString(), location, reason, currentUser.username(), "Impounded");

        if (impoundDao.registerImpoundment(owner, vehicle, impoundment)) {
            logDao.logAction(currentUser.username(), "CREATE_IMPOUND", "Registered impounded vehicle: " + plate);
            System.out.println("Impounded vehicle registered successfully!");
        }
    }

    private void searchRecords() {
        System.out.print("Search by Plate Number, Vehicle ID, or Owner Name: ");
        String keyword = scanner.nextLine().trim();
        var results = impoundDao.searchRecords(keyword);
        if (results.isEmpty()) System.out.println("No matching records found.");
        else results.forEach(System.out::println);
    }

    private void updateVehicle() {
        System.out.print("Enter Vehicle ID to update: ");
        int vehId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("New Plate Number: ");
        String plate = scanner.nextLine().trim();
        System.out.print("New Make: ");
        String make = scanner.nextLine().trim();
        System.out.print("New Model: ");
        String model = scanner.nextLine().trim();
        System.out.print("New Color: ");
        String color = scanner.nextLine().trim();

        if (impoundDao.updateVehicleInfo(vehId, plate, make, model, color)) {
            logDao.logAction(currentUser.username(), "UPDATE_VEHICLE", "Updated Vehicle ID: " + vehId);
            System.out.println("Vehicle details updated.");
        }
    }

    private void updateStatus() {
        System.out.print("Enter Impoundment ID: ");
        int impId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("New Status (Impounded / Pending Release / Released): ");
        String status = scanner.nextLine().trim();

        if (impoundDao.updateImpoundmentStatus(impId, status)) {
            logDao.logAction(currentUser.username(), "UPDATE_STATUS", "Updated Impoundment ID " + impId + " status to " + status);
            System.out.println("Vehicle status updated.");
        }
    }
}