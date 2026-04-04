package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import service.BackupService;
import db.DBConnection;
import security.AuditService;
import java.sql.*;

public class BackupController {

    @FXML private TextArea backupStatus;
    @FXML private TableView<BackupRecord> table;
    @FXML private TableColumn<BackupRecord, String> colFileName;
    @FXML private TableColumn<BackupRecord, Long> colSize;
    @FXML private TableColumn<BackupRecord, Timestamp> colDate;
    @FXML private TableColumn<BackupRecord, Boolean> colVerified;

    private BackupService service;
    private ObservableList<BackupRecord> backupRecords;
    private Integer currentUserId;

    public void setCurrentUser(Integer userId) {
        this.currentUserId = userId;
    }

    @FXML
    public void initialize() {
        service = new BackupService();
        backupRecords = FXCollections.observableArrayList();

        colFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colVerified.setCellValueFactory(new PropertyValueFactory<>("verified"));

        table.setItems(backupRecords);

        refreshHistory();
    }

    public void createBackup() {
        try {
            backupStatus.setText("Creating backup... Please wait.");
            String backupFile = service.createBackup(currentUserId);
            backupStatus.setText("Backup created successfully!\nFile: " + backupFile);

            refreshHistory();

            AuditService.log(currentUserId, "Created database backup: " + backupFile);

        } catch (Exception e) {
            backupStatus.setText("Backup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshHistory() {
        try {
            backupRecords.clear();
            Connection conn = DBConnection.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT * FROM backup_records ORDER BY backup_date DESC"
            );

            while (rs.next()) {
                BackupRecord record = new BackupRecord();
                record.setFileName(rs.getString("backup_file_name"));
                record.setSize(rs.getLong("backup_size"));
                record.setDate(rs.getTimestamp("backup_date"));
                record.setVerified(rs.getBoolean("verified"));
                backupRecords.add(record);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            backupStatus.setText("Failed to load backup history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class BackupRecord {
        private String fileName;
        private long size;
        private Timestamp date;
        private boolean verified;

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }

        public Timestamp getDate() { return date; }
        public void setDate(Timestamp date) { this.date = date; }

        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
    }
}