package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.SecurityUtil;
import com.scc.dao.UserDao;
import com.scc.dao.VehicleImpoundDao;
import com.scc.model.Payment;
import com.scc.model.User;

import java.time.LocalDate;
import java.util.Scanner;

public class AdminMenu {
    private final User currentUser;
    private final UserDao userDao = new UserDao();
    private final VehicleImpoundDao impoundDao = new VehicleImpoundDao();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final ActivityLogViewer logViewer = new ActivityLogViewer();
    private final Scanner scanner = new Scanner(System.in);

    public AdminMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n--- SYSTEM ADMINISTRATOR MENU ---");
            System.out.println("1. Manage User Accounts (Create / Update Role / Deactivate)");
            System.out.println("2. View & Search Impoundment Records");
            System.out.println("3. Process Payment & Release Vehicle");
            System.out.println("4. Delete Duplicate/Incorrect Record");
            System.out.println("5. Archive Old Released Records");
            System.out.println("6. Generate Vehicle Impoundment Summary Report");
            System.out.println("7. View Activity / Audit Logs");
            System.out.println("0. Logout");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> manageUsers();
                case "2" -> viewAndSearch();
                case "3" -> processRelease();
                case "4" -> deleteRecord();
                case "5" -> archiveRecords();
                case "6" -> generateReport();
                case "7" -> {
                    logViewer.displayLogs();
                    logDao.logAction(currentUser.getUsername(), "VIEW_LOGS", "Viewed system audit logs.");
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid selection.");
            }
        }
    }

    private void manageUsers() {
        // FIXED: Cleaned up the broken string and stray '1.' character
        System.out.println("\n1. Create User | 2. Update Role | 3. Deactivate User | 4. List Users");
        String opt = scanner.nextLine().trim();
        switch (opt) {
            case "1" -> {
                System.out.print("New Username: ");
                String uname = scanner.nextLine().trim();
                System.out.print("Password: ");
                String pass = SecurityUtil.hashPassword(scanner.nextLine().trim());
                System.out.print("Role (ADMIN / OFFICER / STAFF): ");
                String role = scanner.nextLine().trim().toUpperCase();
                if (userDao.createUser(uname, pass, role)) {
                    logDao.logAction(currentUser.getUsername(), "CREATE_USER", "Created user: " + uname);
                    System.out.println("User created.");
                }
            }
            case "2" -> {
                System.out.print("User ID: ");
                int id = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("New Role (ADMIN / OFFICER / STAFF): ");
                String role = scanner.nextLine().trim().toUpperCase();
                if (userDao.updateUserRole(id, role)) {
                    logDao.logAction(currentUser.getUsername(), "UPDATE_USER", "Updated user ID " + id + " to " + role);
                    System.out.println("Role updated.");
                }
            }
            case "3" -> {
                System.out.print("User ID to deactivate: ");
                int id = Integer.parseInt(scanner.nextLine().trim());
                if (userDao.deactivateUser(id)) {
                    logDao.logAction(currentUser.getUsername(), "DELETE_USER", "Deactivated user ID " + id);
                    System.out.println("User deactivated.");
                }
            }
            case "4" -> userDao.getAllUsers().forEach(System.out::println);
        }
    }

    private void viewAndSearch() {
        System.out.print("Enter Plate Number, Vehicle ID, or Owner Name (leave blank for all): ");
        String query = scanner.nextLine().trim();
        var list = query.isEmpty() ? impoundDao.getAllRecords() : impoundDao.searchRecords(query);
        if (list.isEmpty()) System.out.println("No records found.");
        else list.forEach(System.out::println);
    }

    private void processRelease() {
        System.out.print("Impoundment ID: ");
        int impId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Payment Amount: ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Payment Method (Cash / Bank Deposit / OTC): ");
        String method = scanner.nextLine().trim();

        Payment payment = new Payment(0, impId, amount, LocalDate.now().toString(), method, "Paid");
        if (impoundDao.recordPaymentAndRelease(payment)) {
            logDao.logAction(currentUser.getUsername(), "PAYMENT_RELEASE", "Recorded payment & released Impound ID: " + impId);
            System.out.println("Payment recorded and vehicle status updated to Released.");
        }
    }

    private void deleteRecord() {
        System.out.print("Enter Impoundment ID to delete: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Confirm Administrator Authorization (YES to confirm): ");
        if ("YES".equalsIgnoreCase(scanner.nextLine().trim())) {
            if (impoundDao.deleteImpoundmentRecord(id)) {
                logDao.logAction(currentUser.getUsername(), "DELETE_RECORD", "Deleted Impoundment ID: " + id);
                System.out.println("Record deleted.");
            }
        }
    }

    private void archiveRecords() {
        System.out.print("Archive released records prior to date (YYYY-MM-DD): ");
        String cutoff = scanner.nextLine().trim();
        if (impoundDao.archiveOldRecords(cutoff)) {
            logDao.logAction(currentUser.getUsername(), "ARCHIVE_RECORDS", "Archived records before " + cutoff);
            System.out.println("Old released records archived.");
        }
    }

    private void generateReport() {
        System.out.print("Start Date (YYYY-MM-DD): ");
        String start = scanner.nextLine().trim();
        System.out.print("End Date (YYYY-MM-DD): ");
        String end = scanner.nextLine().trim();
        impoundDao.generateSummaryReport(start, end);
        logDao.logAction(currentUser.getUsername(), "GENERATE_REPORT", "Generated Impoundment Summary Report.");
    }
}