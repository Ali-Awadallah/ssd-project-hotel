package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.User;
import security.AuditService;
import db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;


import java.sql.*;

public class UserManagementController {

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private ComboBox<String> role;
    @FXML private TableView<User> table;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Integer> colFailedAttempts;
    @FXML private TableColumn<User, Timestamp> colLastLogin;
    @FXML private TableColumn<User, Boolean> colActive;

    private ObservableList<User> userList;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        userList = FXCollections.observableArrayList();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colFailedAttempts.setCellValueFactory(new PropertyValueFactory<>("failedAttempts"));
        colLastLogin.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        table.setItems(userList);

        role.getItems().addAll("Admin", "Manager", "Receptionist");

        loadUsers();
    }

    public void addUser() {
        try {
            String enteredUsername = username.getText().trim();
            String enteredPassword = password.getText().trim();

            if (enteredUsername.isEmpty()) {
                showAlert("Error", "Username is required!");
                return;
            }

            if (enteredPassword.isEmpty()) {
                showAlert("Error", "Password is required!");
                return;
            }

            if (role.getValue() == null) {
                showAlert("Error", "Please select a role!");
                return;
            }

            String hashedPassword = BCrypt.hashpw(enteredPassword, BCrypt.gensalt());

            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES (?, ?, ?)"
            );
            ps.setString(1, enteredUsername);
            ps.setString(2, hashedPassword);
            ps.setString(3, role.getValue());
            ps.executeUpdate();

            AuditService.log(currentUser.getId(), "Created new user: " + enteredUsername);

            showAlert("Success", "User created successfully!");
            clearForm();
            loadUsers();

            ps.close();
            conn.close();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                showAlert("Error", "Username already exists!");
            } else {
                showAlert("Error", "Failed to create user: " + e.getMessage());
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to create user: " + e.getMessage());
        }
    }

    public void resetPassword() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a user!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + selected.getUsername());
        dialog.setContentText("Enter new password:");

        dialog.showAndWait().ifPresent(newPassword -> {
            try {
                String trimmedPassword = newPassword.trim();

                if (trimmedPassword.isEmpty()) {
                    showAlert("Error", "Password cannot be empty!");
                    return;
                }

                String hashedPassword = BCrypt.hashpw(trimmedPassword, BCrypt.gensalt());

                Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE users SET password = ?, failed_attempts = 0, locked_until = NULL WHERE id = ?"
                );
                ps.setString(1, hashedPassword);
                ps.setInt(2, selected.getId());
                ps.executeUpdate();

                AuditService.log(currentUser.getId(), "Reset password for user: " + selected.getUsername());
                showAlert("Success", "Password reset successfully!");

                ps.close();
                conn.close();

            } catch (Exception e) {
                showAlert("Error", "Failed to reset password: " + e.getMessage());
            }
        });
    }

    public void toggleActive() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a user!");
            return;
        }

        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET is_active = NOT is_active WHERE id = ?"
            );
            ps.setInt(1, selected.getId());
            ps.executeUpdate();

            AuditService.log(
                    currentUser.getId(),
                    (selected.isActive() ? "Disabled" : "Enabled") + " user: " + selected.getUsername()
            );

            loadUsers();
            ps.close();
            conn.close();

        } catch (Exception e) {
            showAlert("Error", "Failed to update user: " + e.getMessage());
        }
    }

    public void deleteUser() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a user!");
            return;
        }

        if (selected.getId() == currentUser.getId()) {
            showAlert("Error", "You cannot delete your own account!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Are you sure you want to delete user: " + selected.getUsername() + "?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?");
                ps.setInt(1, selected.getId());
                ps.executeUpdate();

                AuditService.log(currentUser.getId(), "Deleted user: " + selected.getUsername());
                showAlert("Success", "User deleted successfully!");
                loadUsers();

                ps.close();
                conn.close();

            } catch (Exception e) {
                showAlert("Error", "Failed to delete user: " + e.getMessage());
            }
        }
    }

    public void refresh() {
        loadUsers();
    }

    private void loadUsers() {
        try {
            userList.clear();
            Connection conn = DBConnection.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users ORDER BY id");

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setFailedAttempts(rs.getInt("failed_attempts"));
                user.setLastLogin(rs.getTimestamp("last_login"));
                user.setActive(rs.getBoolean("is_active"));
                userList.add(user);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void clearForm() {
        username.clear();
        password.clear();
        role.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert.AlertType type = title.equalsIgnoreCase("Error")
                ? Alert.AlertType.ERROR
                : Alert.AlertType.INFORMATION;

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText("Message");
        alert.setContentText(message);
        alert.showAndWait();
    }
}