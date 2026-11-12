package com.scc.model;

public record User(int id, String username, String password, String role, String status) {

    // Compatibility helper methods for code looking for getter methods
    public String getUsername() {
        return this.username;
    }

    public String getRole() {
        return this.role;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - status: %s", id, username, role, status);
    }
}