package com.scc.model;

public class Impoundment {
    private int impoundmentId;
    private int vehicleId;
    private String impoundDate;
    private String impoundLocation;
    private String impoundReason;
    private String impoundOfficer;
    private String vehicleStatus; // Impounded, Pending Release, Released, Archived

    public Impoundment() {}

    public Impoundment(int impoundmentId, int vehicleId, String impoundDate, String impoundLocation,
                       String impoundReason, String impoundOfficer, String vehicleStatus) {
        this.impoundmentId = impoundmentId;
        this.vehicleId = vehicleId;
        this.impoundDate = impoundDate;
        this.impoundLocation = impoundLocation;
        this.impoundReason = impoundReason;
        this.impoundOfficer = impoundOfficer;
        this.vehicleStatus = vehicleStatus;
    }

    public int getImpoundmentId() { return impoundmentId; }
    public void setImpoundmentId(int impoundmentId) { this.impoundmentId = impoundmentId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getImpoundDate() { return impoundDate; }
    public void setImpoundDate(String impoundDate) { this.impoundDate = impoundDate; }

    public String getImpoundLocation() { return impoundLocation; }
    public void setImpoundLocation(String impoundLocation) { this.impoundLocation = impoundLocation; }

    public String getImpoundReason() { return impoundReason; }
    public void setImpoundReason(String impoundReason) { this.impoundReason = impoundReason; }

    public String getImpoundOfficer() { return impoundOfficer; }
    public void setImpoundOfficer(String impoundOfficer) { this.impoundOfficer = impoundOfficer; }

    public String getVehicleStatus() { return vehicleStatus; }
    public void setVehicleStatus(String vehicleStatus) { this.vehicleStatus = vehicleStatus; }
}