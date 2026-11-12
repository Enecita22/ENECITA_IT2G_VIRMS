package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.SecurityUtil;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner scanner = new Scanner(System.in);

    // Main authentication entry point with menu options
    public User showAuthMenu() {
        while (true) {
            System.out.println("\n=== VEHICLE IMPOUND RECORDS MANAGEMENT SYSTEM ===");
            System.out.println("1. Login");
            System.out.println("2. Register Account");
            System.out.println("0. Exit");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    User user = login();
                    if (user != null) {
                        return user; // Returns the logged-in user to Main
                    }
                }
                case "2" -> registerUser();
                case "0" -> {
                    System.out.println("Exiting system. Goodbye!");
                    return null;
                }
                default -> System.out.println("Invalid selection. Please try again.");
            }
        }
    }

    public User login() {
        System.out.println("\n--- USER LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        String hashedPassword = SecurityUtil.hashPassword(password);
        User user = userDao.authenticate(username, hashedPassword);

        if (user != null) {
            logDao.logAction(user.getUsername(), "LOGIN", "User logged into the system.");
            System.out.println("Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
            return user;
        } else {
            System.out.println("Invalid credentials or inactive account.");
            return null;
        }
    }

    private void registerUser() {
        System.out.println("\n--- USER REGISTRATION ---");
        System.out.print("Enter new Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Role (ADMIN / OFFICER / STAFF): ");
        String role = scanner.nextLine().trim().toUpperCase();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        String hashedPassword = SecurityUtil.hashPassword(password);
        boolean success = userDao.createUser(username, hashedPassword, role);

        if (success) {
            System.out.println("Registration successful! You can now log in.");
            logDao.logAction(username, "REGISTER", "New user account registered: " + username);
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    public void logout(User user) {
        if (user != null) {
            logDao.logAction(user.getUsername(), "LOGOUT", "User logged out of the system.");
            System.out.println("Logged out successfully.");
        }
    }
}