package com.scc.model;

public class VehicleOwner {
    private int ownerId;
    private String ownerName;
    private String contactNumber;
    private String address;
    private String email;

    public VehicleOwner() {}

    public VehicleOwner(int ownerId, String ownerName, String contactNumber, String address, String email) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.contactNumber = contactNumber;
        this.address = address;
        this.email = email;
    }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}