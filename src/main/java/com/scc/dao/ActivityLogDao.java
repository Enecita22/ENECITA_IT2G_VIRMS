package com.scc.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // First parameter must be String username to match the menus
    public void logAction(String username, String actionType, String description) {
        String createTable = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                action_type TEXT NOT NULL,
                description TEXT NOT NULL,
                timestamp TEXT NOT NULL
            );
        """;

        String insertLog = "INSERT INTO activity_logs (username, action_type, description, timestamp) VALUES (?, ?, ?, ?)";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTable);
            try (PreparedStatement pstmt = conn.prepareStatement(insertLog)) {
                pstmt.setString(1, username);
                pstmt.setString(2, actionType);
                pstmt.setString(3, description);
                pstmt.setString(4, timestamp);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error recording audit log: " + e.getMessage());
        }
    }

    public List<String> getAllLogs() {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT username, action_type, description, timestamp FROM activity_logs ORDER BY log_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(String.format("[%s] User: %s | Action: %s - %s",
                        rs.getString("timestamp"),
                        rs.getString("username"),
                        rs.getString("action_type"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            // Table might not exist yet
        }
        return logs;
    }
}