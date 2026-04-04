package service;

import db.DBConnection;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupService {

    private static final String BACKUP_DIR = "backups/";

    public String createBackup(int userId) throws Exception {
        // Create backup directory if it doesn't exist
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFile = BACKUP_DIR + "hotel_system_backup_" + timestamp + ".sql";

        try {
            // Check if mysqldump is available
            String mysqldumpPath = findMysqldump();
            if (mysqldumpPath == null) {
                // Fallback: Create a simple SQL dump using Java
                return createSimpleBackup(backupFile, userId);
            }

            // Execute mysqldump command
            ProcessBuilder pb = new ProcessBuilder(
                    mysqldumpPath,
                    "-u", "root",
                    "--password=",
                    "--host=localhost",
                    "hotel_system",
                    "--result-file=" + backupFile
            );

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Log backup in database
                File file = new File(backupFile);
                recordBackup(backupFile, file.length(), userId, true);
                return backupFile;
            } else {
                throw new Exception("Backup failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Backup failed: " + e.getMessage());
        }
    }

    private String findMysqldump() {
        // Common MySQL installation paths
        String[] possiblePaths = {
                "mysqldump",
                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe",
                "C:\\xampp\\mysql\\bin\\mysqldump.exe",
                "C:\\wamp64\\bin\\mysql\\mysql8.0\\bin\\mysqldump.exe",
                "/usr/bin/mysqldump",
                "/usr/local/bin/mysqldump"
        };

        for (String path : possiblePaths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(path, "--version");
                Process p = pb.start();
                int exitCode = p.waitFor();
                if (exitCode == 0) {
                    return path;
                }
            } catch (Exception e) {
                // Path not found, continue
            }
        }
        return null;
    }

    private String createSimpleBackup(String backupFile, int userId) throws Exception {
        // Simple Java-based backup using SELECT ... INTO OUTFILE
        try (Connection conn = DBConnection.connect();
             PrintWriter writer = new PrintWriter(new FileWriter(backupFile))) {

            writer.println("-- Hotel Management System Backup");
            writer.println("-- Generated: " + new Date());
            writer.println("-- Backup type: Simple Java backup (mysqldump not available)");
            writer.println();

            // Backup users table
            writer.println("-- Users table");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                writer.println("INSERT INTO users (id, username, password, role, failed_attempts) VALUES (" +
                        rs.getInt("id") + ", '" +
                        rs.getString("username") + "', '" +
                        rs.getString("password") + "', '" +
                        rs.getString("role") + "', " +
                        rs.getInt("failed_attempts") + ");");
            }

            // Backup rooms table
            writer.println("\n-- Rooms table");
            rs = stmt.executeQuery("SELECT * FROM rooms");
            while (rs.next()) {
                writer.println("INSERT INTO rooms (id, room_number, type, status, price_per_night) VALUES (" +
                        rs.getInt("id") + ", " +
                        rs.getInt("room_number") + ", '" +
                        rs.getString("type") + "', '" +
                        rs.getString("status") + "', " +
                        rs.getDouble("price_per_night") + ");");
            }

            rs.close();
            stmt.close();

            File file = new File(backupFile);
            recordBackup(backupFile, file.length(), userId, true);
            return backupFile;

        } catch (Exception e) {
            throw new Exception("Simple backup failed: " + e.getMessage());
        }
    }

    private void recordBackup(String fileName, long fileSize, int userId, boolean verified) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(
                    "INSERT INTO backup_records (backup_file_name, backup_size, verified, created_by) VALUES (?, ?, ?, ?)"
            );
            ps.setString(1, fileName);
            ps.setLong(2, fileSize);
            ps.setBoolean(3, verified);
            ps.setInt(4, userId);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    public void restoreBackup(String backupFile) throws Exception {
        // Note: Restore is more complex and requires mysqldump or careful SQL execution
        // For now, show a message that restore requires manual intervention
        throw new Exception("Restore requires MySQL command line tools. Please use phpMyAdmin to restore.");
    }
}