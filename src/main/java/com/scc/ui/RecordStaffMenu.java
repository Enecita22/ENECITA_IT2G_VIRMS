package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.VehicleImpoundDao;
import com.scc.model.Impoundment;
import com.scc.model.Payment;
import com.scc.model.User;
import com.scc.model.Vehicle;
import com.scc.model.VehicleOwner;

import java.time.LocalDate;
import java.util.Scanner;

public class RecordStaffMenu {
    private final User currentUser;
    private final VehicleImpoundDao impoundDao = new VehicleImpoundDao();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner scanner = new Scanner(System.in);

    public RecordStaffMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n--- RECORDS STAFF MENU ---");
            System.out.println("1. Add Vehicle & Owner Record");
            System.out.println("2. View All Vehicle Records");
            System.out.println("3. Search Records (Plate / Vehicle ID / Owner)");
            System.out.println("4. Update Owner Information");
            System.out.println("5. Update Vehicle Information");
            System.out.println("6. Record Payment & Vehicle Release");
            System.out.println("7. Generate Impoundment Summary Report");
            System.out.println("0. Logout");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addRecord();
                case "2" -> impoundDao.getAllRecords().forEach(System.out::println);
                case "3" -> searchRecords();
                case "4" -> updateOwner();
                case "5" -> updateVehicle();
                case "6" -> recordRelease();
                case "7" -> generateReport();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid selection. Please choose between 0 and 7.");
            }
        }
    }

    private void addRecord() {
        try {
            System.out.println("\n--- Add Vehicle & Owner Record ---");
            System.out.print("Owner Name: ");
            String ownerName = scanner.nextLine().trim();
            System.out.print("Contact Number: ");
            String contact = scanner.nextLine().trim();
            System.out.print("Address: ");
            String address = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Plate Number: ");
            String plate = scanner.nextLine().trim();
            System.out.print("Vehicle Type: ");
            String type = scanner.nextLine().trim();
            System.out.print("Make: ");
            String make = scanner.nextLine().trim();
            System.out.print("Model: ");
            String model = scanner.nextLine().trim();
            System.out.print("Color: ");
            String color = scanner.nextLine().trim();

            System.out.print("Impound Location: ");
            String location = scanner.nextLine().trim();
            System.out.print("Impound Reason: ");
            String reason = scanner.nextLine().trim();
            System.out.print("Apprehending Officer: ");
            String officer = scanner.nextLine().trim();

            if (ownerName.isEmpty() || plate.isEmpty()) {
                System.out.println("Error: Owner Name and Plate Number cannot be empty.");
                return;
            }

            VehicleOwner owner = new VehicleOwner(0, ownerName, contact, address, email);
            Vehicle vehicle = new Vehicle(0, 0, plate, type, make, model, color);
            Impoundment impoundment = new Impoundment(0, 0, LocalDate.now().toString(), location, reason, officer, "Impounded");

            if (impoundDao.registerImpoundment(owner, vehicle, impoundment)) {
                logDao.logAction(currentUser.username(), "CREATE_RECORD", "Staff added record for plate: " + plate);
                System.out.println("Record saved successfully.");
            } else {
                System.out.println("Failed to save record. Check if the plate number already exists.");
            }
        } catch (Exception e) {
            System.out.println("An error occurred while adding the record: " + e.getMessage());
        }
    }

    private void searchRecords() {
        System.out.print("Enter Plate Number, Vehicle ID, or Owner Name: ");
        String keyword = scanner.nextLine().trim();
        var results = impoundDao.searchRecords(keyword);
        if (results.isEmpty()) {
            System.out.println("No records found.");
        } else {
            results.forEach(System.out::println);
        }
    }

    private void updateOwner() {
        try {
            System.out.print("Enter Vehicle ID associated with the owner: ");
            String inputId = scanner.nextLine().trim();
            if (inputId.isEmpty()) return;
            int vehId = Integer.parseInt(inputId);

            System.out.print("Updated Owner Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Updated Contact Number: ");
            String contact = scanner.nextLine().trim();
            System.out.print("Updated Address: ");
            String address = scanner.nextLine().trim();
            System.out.print("Updated Email: ");
            String email = scanner.nextLine().trim();

            if (impoundDao.updateOwnerInfo(vehId, name, contact, address, email)) {
                logDao.logAction(currentUser.username(), "UPDATE_OWNER", "Updated owner details for Vehicle ID: " + vehId);
                System.out.println("Owner information updated.");
            } else {
                System.out.println("Update failed. Vehicle ID may not exist.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input format. Please enter a valid numeric Vehicle ID.");
        } catch (Exception e) {
            System.out.println("Error updating owner: " + e.getMessage());
        }
    }

    private void updateVehicle() {
        try {
            System.out.print("Enter Vehicle ID: ");
            String inputId = scanner.nextLine().trim();
            if (inputId.isEmpty()) return;
            int vehId = Integer.parseInt(inputId);

            System.out.print("Updated Plate Number: ");
            String plate = scanner.nextLine().trim();
            System.out.print("Updated Make: ");
            String make = scanner.nextLine().trim();
            System.out.print("Updated Model: ");
            String model = scanner.nextLine().trim();
            System.out.print("Updated Color: ");
            String color = scanner.nextLine().trim();

            if (impoundDao.updateVehicleInfo(vehId, plate, make, model, color)) {
                logDao.logAction(currentUser.username(), "UPDATE_VEHICLE", "Staff updated Vehicle ID: " + vehId);
                System.out.println("Vehicle information updated.");
            } else {
                System.out.println("Update failed. Vehicle ID may not exist.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input format. Please enter a valid numeric Vehicle ID.");
        } catch (Exception e) {
            System.out.println("Error updating vehicle: " + e.getMessage());
        }
    }

    private void recordRelease() {
        try {
            System.out.print("Impoundment ID: ");
            String impInput = scanner.nextLine().trim();
            if (impInput.isEmpty()) return;
            int impId = Integer.parseInt(impInput);

            System.out.print("Payment Amount: ");
            String amtInput = scanner.nextLine().trim();
            if (amtInput.isEmpty()) return;
            double amount = Double.parseDouble(amtInput);

            System.out.print("Payment Method (Cash / Over-the-Counter): ");
            String method = scanner.nextLine().trim();

            Payment payment = new Payment(0, impId, amount, LocalDate.now().toString(), method, "Paid");
            if (impoundDao.recordPaymentAndRelease(payment)) {
                logDao.logAction(currentUser.username(), "RECORD_RELEASE", "Recorded release & payment for Impound ID: " + impId);
                System.out.println("Release and payment recorded.");
            } else {
                System.out.println("Failed to record release. Verify if the Impoundment ID exists.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format entered for ID or Payment Amount.");
        } catch (Exception e) {
            System.out.println("Error recording release: " + e.getMessage());
        }
    }

    private void generateReport() {
        try {
            System.out.print("Start Date (YYYY-MM-DD): ");
            String start = scanner.nextLine().trim();
            System.out.print("End Date (YYYY-MM-DD): ");
            String end = scanner.nextLine().trim();

            if (start.isEmpty() || end.isEmpty()) {
                System.out.println("Dates cannot be empty.");
                return;
            }

            impoundDao.generateSummaryReport(start, end);
            logDao.logAction(currentUser.username(), "GENERATE_REPORT", "Staff generated summary report.");
        } catch (Exception e) {
            System.out.println("Error generating report: " + e.getMessage());
        }
    }
}