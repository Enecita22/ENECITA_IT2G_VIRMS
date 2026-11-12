package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        String usersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL,
                is_active INTEGER DEFAULT 1
            );
        """;

        String ownerTable = """
            CREATE TABLE IF NOT EXISTS vehicle_owner (
                owner_id INTEGER PRIMARY KEY AUTOINCREMENT,
                owner_name TEXT NOT NULL,
                contact_number TEXT NOT NULL,
                address TEXT NOT NULL,
                email TEXT
            );
        """;

        String vehicleTable = """
            CREATE TABLE IF NOT EXISTS vehicle (
                vehicle_id INTEGER PRIMARY KEY AUTOINCREMENT,
                owner_id INTEGER NOT NULL,
                plate_number TEXT UNIQUE NOT NULL,
                vehicle_type TEXT NOT NULL,
                make TEXT NOT NULL,
                model TEXT NOT NULL,
                color TEXT NOT NULL,
                FOREIGN KEY (owner_id) REFERENCES vehicle_owner(owner_id)
            );
        """;

        String impoundmentTable = """
            CREATE TABLE IF NOT EXISTS impoundment (
                impoundment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                vehicle_id INTEGER NOT NULL,
                impound_date TEXT NOT NULL,
                impound_location TEXT NOT NULL,
                impound_reason TEXT NOT NULL,
                impound_officer TEXT NOT NULL,
                vehicle_status TEXT NOT NULL,
                FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id)
            );
        """;

        String paymentTable = """
            CREATE TABLE IF NOT EXISTS payment (
                payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                impoundment_id INTEGER NOT NULL,
                payment_amount REAL NOT NULL,
                payment_date TEXT NOT NULL,
                payment_method TEXT NOT NULL,
                payment_status TEXT NOT NULL,
                FOREIGN KEY (impoundment_id) REFERENCES impoundment(impoundment_id)
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(usersTable);
            stmt.execute(ownerTable);
            stmt.execute(vehicleTable);
            stmt.execute(impoundmentTable);
            stmt.execute(paymentTable);
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
        // Inside DatabaseInitializer.java, after creating the users table, add this:
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE users ADD COLUMN status TEXT DEFAULT 'Active';");
        } catch (SQLException e) {
            // Column might already exist, which is fine to ignore
        }
    }
}