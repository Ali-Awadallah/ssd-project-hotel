package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import security.AuthService;
import model.User;
import db.DBConnection;
import java.sql.*;

public class LoginController {

    @FXML private TextField username;
    @FXML private PasswordField password;

    public void login() {
        try {
            if (username.getText().trim().isEmpty()) {
                showAlert("Error", "Username is required!");
                return;
            }

            if (password.getText().trim().isEmpty()) {
                showAlert("Error", "Password is required!");
                return;
            }

            AuthService auth = new AuthService();
            User user = auth.login(username.getText().trim(), password.getText().trim());

            if (user != null) {
                // Update last login timestamp
                updateLastLogin(user.getId());

                showAlert("Success", "Login successful! Welcome, " + user.getUsername());

                // Load dashboard with user info
                Stage stage = (Stage) username.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
                Scene scene = new Scene(loader.load());

                // Pass user info to dashboard
                DashboardController dashboardController = loader.getController();
                dashboardController.setCurrentUser(user);

                stage.setScene(scene);
                stage.setTitle("Hotel System - Dashboard (" + user.getRole() + ")");
                stage.setMaximized(true);
                stage.show();

            } else {
                showAlert("Error", "Invalid username or password");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "System error: " + e.getMessage());
        }
    }

    private void updateLastLogin(int userId) {
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET last_login = NOW() WHERE id = ?"
            );
            ps.setInt(1, userId);
            ps.executeUpdate();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}