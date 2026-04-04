package model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private int failedAttempts;
    private Timestamp lockedUntil;
    private Timestamp lastLogin;
    private boolean isActive;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public Timestamp getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Timestamp lockedUntil) { this.lockedUntil = lockedUntil; }

    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean canSearchRooms() {
        return role.equals("Admin") || role.equals("Receptionist");
    }

    public boolean canManageReservations() {
        return role.equals("Admin") || role.equals("Receptionist");
    }

    public boolean canManagePayments() {
        return role.equals("Admin") || role.equals("Receptionist");
    }

    public boolean canViewReports() {
        return role.equals("Admin") || role.equals("Manager");
    }

    public boolean canManageUsers() {
        return role.equals("Admin");
    }

    public boolean canManageBackups() {
        return role.equals("Admin");
    }

    public boolean canManageServiceRequests() {
        return role.equals("Admin") || role.equals("Manager");
    }

    public String getDisplayName() {
        return username + " (" + role + ")";
    }
}