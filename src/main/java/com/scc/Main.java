package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.model.User;
import com.scc.ui.AdminMenu;
import com.scc.ui.Authentication;
import com.scc.ui.OfficerMenu;

public class Main {
    public static void main(String[] args) {
        System.out.println("Vehicle Impound Records Management System is running...");

        // 1. Initialize tables and constraints
        DatabaseInitializer.initialize();

        // 2. Launch authentication menu (Login / Register / Exit)
        Authentication auth = new Authentication();
        User loggedInUser = auth.showAuthMenu(); // <-- Make sure this is showAuthMenu()

        if (loggedInUser != null) {
            String role = loggedInUser.role().toUpperCase();

            // Route based on role
            switch (role) {
                case "ADMIN":
                    AdminMenu adminMenu = new AdminMenu(loggedInUser);
                    adminMenu.showMenu();
                    break;
                case "OFFICER":
                    OfficerMenu officerMenu = new OfficerMenu(loggedInUser);
                    officerMenu.showMenu();
                    break;
                case "STAFF":
                case "RECORD_STAFF":
                    System.out.println("Staff menu loaded successfully.");
                    break;
                default:
                    System.out.println("Unknown user role. Access denied.");
            }

            // Logout when exiting the menu loop
            auth.logout(loggedInUser);
        } else {
            System.out.println("Exiting system.");
        }
    }
}