package com.scc.dao;

import com.scc.model.Impoundment;
import com.scc.model.Payment;
import com.scc.model.Vehicle;
import com.scc.model.VehicleOwner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleImpoundDao {

    // CREATE: Register Owner, Vehicle, and Impoundment in one transaction
    public boolean registerImpoundment(VehicleOwner owner, Vehicle vehicle, Impoundment impoundment) {
        String insertOwner = "INSERT INTO vehicle_owner (owner_name, contact_number, address, email) VALUES (?, ?, ?, ?)";
        String insertVehicle = "INSERT INTO vehicle (owner_id, plate_number, vehicle_type, make, model, color) VALUES (?, ?, ?, ?, ?, ?)";
        String insertImpound = "INSERT INTO impoundment (vehicle_id, impound_date, impound_location, impound_reason, impound_officer, vehicle_status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insert Owner
                int ownerId;
                try (PreparedStatement psOwner = conn.prepareStatement(insertOwner, Statement.RETURN_GENERATED_KEYS)) {
                    psOwner.setString(1, owner.getOwnerName());
                    psOwner.setString(2, owner.getContactNumber());
                    psOwner.setString(3, owner.getAddress());
                    psOwner.setString(4, owner.getEmail());
                    psOwner.executeUpdate();
                    ResultSet rsOwner = psOwner.getGeneratedKeys();
                    if (!rsOwner.next()) throw new SQLException("Failed to retrieve owner ID.");
                    ownerId = rsOwner.getInt(1);
                }

                // 2. Insert Vehicle
                int vehicleId;
                try (PreparedStatement psVeh = conn.prepareStatement(insertVehicle, Statement.RETURN_GENERATED_KEYS)) {
                    psVeh.setInt(1, ownerId);
                    psVeh.setString(2, vehicle.getPlateNumber().toUpperCase());
                    psVeh.setString(3, vehicle.getVehicleType());
                    psVeh.setString(4, vehicle.getMake());
                    psVeh.setString(5, vehicle.getModel());
                    psVeh.setString(6, vehicle.getColor());
                    psVeh.executeUpdate();
                    ResultSet rsVeh = psVeh.getGeneratedKeys();
                    if (!rsVeh.next()) throw new SQLException("Failed to retrieve vehicle ID.");
                    vehicleId = rsVeh.getInt(1);
                }

                // 3. Insert Impoundment Record
                try (PreparedStatement psImp = conn.prepareStatement(insertImpound)) {
                    psImp.setInt(1, vehicleId);
                    psImp.setString(2, impoundment.getImpoundDate());
                    psImp.setString(3, impoundment.getImpoundLocation());
                    psImp.setString(4, impoundment.getImpoundReason());
                    psImp.setString(5, impoundment.getImpoundOfficer());
                    psImp.setString(6, impoundment.getVehicleStatus());
                    psImp.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Transaction failed (possible duplicate plate): " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return false;
        }
    }

    // CREATE: Record Payment & Release
    public boolean recordPaymentAndRelease(Payment payment) {
        String insertPay = "INSERT INTO payment (impoundment_id, payment_amount, payment_date, payment_method, payment_status) VALUES (?, ?, ?, ?, ?)";
        String updateStatus = "UPDATE impoundment SET vehicle_status = 'Released' WHERE impoundment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psPay = conn.prepareStatement(insertPay);
                 PreparedStatement psStat = conn.prepareStatement(updateStatus)) {

                psPay.setInt(1, payment.getImpoundmentId());
                psPay.setDouble(2, payment.getPaymentAmount());
                psPay.setString(3, payment.getPaymentDate());
                psPay.setString(4, payment.getPaymentMethod());
                psPay.setString(5, payment.getPaymentStatus());
                psPay.executeUpdate();

                if ("Paid".equalsIgnoreCase(payment.getPaymentStatus())) {
                    psStat.setInt(1, payment.getImpoundmentId());
                    psStat.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Payment/Release error: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    // READ: View All Impounded Vehicles
    public List<String> getAllRecords() {
        List<String> records = new ArrayList<>();
        String sql = """
            SELECT i.impoundment_id, v.vehicle_id, v.plate_number, v.vehicle_type, v.make, v.model, v.color,
                   o.owner_name, o.contact_number, i.impound_date, i.impound_location, i.impound_reason, i.vehicle_status
            FROM impoundment i
            JOIN vehicle v ON i.vehicle_id = v.vehicle_id
            JOIN vehicle_owner o ON v.owner_id = o.owner_id
            WHERE i.vehicle_status != 'Archived'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                records.add(formatRecordRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving records: " + e.getMessage());
        }
        return records;
    }

    // READ: Search by Plate Number, Vehicle ID, or Owner's Name
    public List<String> searchRecords(String keyword) {
        List<String> results = new ArrayList<>();
        String sql = """
            SELECT i.impoundment_id, v.vehicle_id, v.plate_number, v.vehicle_type, v.make, v.model, v.color,
                   o.owner_name, o.contact_number, i.impound_date, i.impound_location, i.impound_reason, i.vehicle_status
            FROM impoundment i
            JOIN vehicle v ON i.vehicle_id = v.vehicle_id
            JOIN vehicle_owner o ON v.owner_id = o.owner_id
            WHERE v.plate_number LIKE ? OR CAST(v.vehicle_id AS TEXT) = ? OR o.owner_name LIKE ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, keyword);
            pstmt.setString(3, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(formatRecordRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
        return results;
    }

    // UPDATE: Vehicle Info
    public boolean updateVehicleInfo(int vehicleId, String plate, String make, String model, String color) {
        String sql = "UPDATE vehicle SET plate_number = ?, make = ?, model = ?, color = ? WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plate.toUpperCase());
            pstmt.setString(2, make);
            pstmt.setString(3, model);
            pstmt.setString(4, color);
            pstmt.setInt(5, vehicleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
            return false;
        }
    }

    // UPDATE: Owner Info
    public boolean updateOwnerInfo(int vehicleId, String ownerName, String contact, String address, String email) {
        String sql = """
            UPDATE vehicle_owner SET owner_name = ?, contact_number = ?, address = ?, email = ?
            WHERE owner_id = (SELECT owner_id FROM vehicle WHERE vehicle_id = ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ownerName);
            pstmt.setString(2, contact);
            pstmt.setString(3, address);
            pstmt.setString(4, email);
            pstmt.setInt(5, vehicleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating owner: " + e.getMessage());
            return false;
        }
    }

    // UPDATE: Impoundment Details & Status
    public boolean updateImpoundmentStatus(int impoundmentId, String newStatus) {
        String sql = "UPDATE impoundment SET vehicle_status = ? WHERE impoundment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, impoundmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating status: " + e.getMessage());
            return false;
        }
    }

    // DELETE: Remove duplicate/incorrect record (Admin authorized)
    public boolean deleteImpoundmentRecord(int impoundmentId) {
        String sql = "DELETE FROM impoundment WHERE impoundment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, impoundmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete error: " + e.getMessage());
            return false;
        }
    }

    // DELETE / ARCHIVE: Archive old records
    public boolean archiveOldRecords(String cutoffDate) {
        String sql = "UPDATE impoundment SET vehicle_status = 'Archived' WHERE impound_date < ? AND vehicle_status = 'Released'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cutoffDate);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Archive error: " + e.getMessage());
            return false;
        }
    }

    // REPORT: Vehicle Impoundment Summary Report
    public void generateSummaryReport(String startDate, String endDate) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("\n=== VEHICLE IMPOUNDMENT SUMMARY REPORT ===");

            // 1. Total Impounded Vehicles
            ResultSet rsTotal = stmt.executeQuery("SELECT COUNT(*) AS total FROM impoundment");
            if (rsTotal.next()) {
                System.out.println("Total Recorded Impoundments: " + rsTotal.getInt("total"));
            }

            // 2. Grouped by Vehicle Status
            System.out.println("\n--- Grouped by Vehicle Status ---");
            ResultSet rsStatus = stmt.executeQuery("SELECT vehicle_status, COUNT(*) AS cnt FROM impoundment GROUP BY vehicle_status");
            while (rsStatus.next()) {
                System.out.printf("  %-18s : %d%n", rsStatus.getString("vehicle_status"), rsStatus.getInt("cnt"));
            }

            // 3. Grouped by Impoundment Location
            System.out.println("\n--- Grouped by Impoundment Location ---");
            ResultSet rsLoc = stmt.executeQuery("SELECT impound_location, COUNT(*) AS cnt FROM impoundment GROUP BY impound_location");
            while (rsLoc.next()) {
                System.out.printf("  %-18s : %d%n", rsLoc.getString("impound_location"), rsLoc.getInt("cnt"));
            }

            // 4. Within Selected Date Range
            String rangeSql = "SELECT COUNT(*) AS range_cnt FROM impoundment WHERE impound_date BETWEEN ? AND ?";
            try (PreparedStatement psRange = conn.prepareStatement(rangeSql)) {
                psRange.setString(1, startDate);
                psRange.setString(2, endDate);
                ResultSet rsRange = psRange.executeQuery();
                if (rsRange.next()) {
                    System.out.printf("\nVehicles Impounded (%s to %s): %d%n", startDate, endDate, rsRange.getInt("range_cnt"));
                }
            }
            System.out.println("==========================================\n");

        } catch (SQLException e) {
            System.err.println("Report generation error: " + e.getMessage());
        }
    }

    private String formatRecordRow(ResultSet rs) throws SQLException {
        return String.format(
                "ImpoundID: %d | VehID: %d | Plate: %s (%s %s %s - %s) | Owner: %s (%s) | Date: %s | Loc: %s | Reason: %s | Status: [%s]",
                rs.getInt("impoundment_id"),
                rs.getInt("vehicle_id"),
                rs.getString("plate_number"),
                rs.getString("color"),
                rs.getString("make"),
                rs.getString("model"),
                rs.getString("vehicle_type"),
                rs.getString("owner_name"),
                rs.getString("contact_number"),
                rs.getString("impound_date"),
                rs.getString("impound_location"),
                rs.getString("impound_reason"),
                rs.getString("vehicle_status")
        );
    }
}