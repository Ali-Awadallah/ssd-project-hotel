package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import model.User;

public class DashboardController {

    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        setupRoleBasedMenu();
        showHome();
    }

    private void setupRoleBasedMenu() {
        if (sidebar == null) {
            System.err.println("Sidebar is null! Check FXML fx:id='sidebar'");
            return;
        }

        sidebar.getChildren().clear();

        Label welcomeLabel = new Label("Welcome,\n" + currentUser.getUsername());
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0 0 20 0;");
        welcomeLabel.setWrapText(true);
        sidebar.getChildren().add(welcomeLabel);

        addMenuButton("Dashboard", this::showHome, "#2980b9");

        if (currentUser.canSearchRooms()) {
            addMenuButton("Search Rooms", this::openRoomSearch, "#3498db");
        }

        if (currentUser.canManageReservations()) {
            addMenuButton("Reservations", this::openReservation, "#27ae60");
        }

        if (currentUser.canManagePayments()) {
            addMenuButton("Payments", this::openPayment, "#8e44ad");
        }

        if (currentUser.canManageServiceRequests()) {
            addMenuButton("Service Requests", this::openServiceRequests, "#f39c12");
        }

        if (currentUser.canViewReports()) {
            addMenuButton("Reports", this::openReports, "#e67e22");
        }

        if (currentUser.canManageUsers()) {
            addMenuButton("User Management", this::openUserManagement, "#e74c3c");
        }

        if (currentUser.canManageBackups()) {
            addMenuButton("Backup & Restore", this::openBackup, "#16a085");
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        addMenuButton("Logout", this::logout, "#c0392b");
    }

    private void addMenuButton(String text, Runnable action, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 10; -fx-cursor: hand; -fx-background-radius: 5;");
        btn.setOnAction(e -> action.run());
        sidebar.getChildren().add(btn);
        VBox.setMargin(btn, new Insets(5, 0, 5, 0));
    }

    public void showHome() {
        contentArea.getChildren().clear();
        VBox homeContent = new VBox(20);
        homeContent.setStyle("-fx-alignment: center; -fx-padding: 50;");

        Label welcomeLabel = new Label("Welcome to Hotel Management System");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label userInfo = new Label("Logged in as: " + currentUser.getDisplayName());
        userInfo.setStyle("-fx-font-size: 16px;");

        Label roleInfo = new Label("Your Role: " + currentUser.getRole());
        roleInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        homeContent.getChildren().addAll(welcomeLabel, userInfo, roleInfo);
        contentArea.getChildren().add(homeContent);
    }

    public void openRoomSearch() {
        if (!currentUser.canSearchRooms()) {
            showAccessDenied();
            return;
        }
        loadPage("/view/room_search.fxml");
    }

    public void openReservation() {
        if (!currentUser.canManageReservations()) {
            showAccessDenied();
            return;
        }
        loadPage("/view/reservation.fxml");
    }

    public void openPayment() {
        if (!currentUser.canManagePayments()) {
            showAccessDenied();
            return;
        }
        loadPage("/view/payment.fxml");
    }

    public void openServiceRequests() {
        if (!currentUser.canManageServiceRequests()) {
            showAccessDenied();
            return;
        }
        loadPage("/view/service_requests.fxml");
    }

    public void openReports() {
        if (!currentUser.canViewReports()) {
            showAccessDenied();
            return;
        }
        loadPage("/view/reports.fxml");
    }

    public void openUserManagement() {
        if (!currentUser.canManageUsers()) {
            showAccessDenied();
            return;
        }
        loadPage("/view/user_management.fxml");
    }

    public void openBackup() {
        if (!currentUser.canManageBackups()) {
            showAccessDenied();
            return;
        }
        loadPage("/view/backup.fxml");
    }

    public void logout() {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Logout");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to logout?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Hotel System - Login");
                stage.setMaximized(false);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not logout: " + e.getMessage());
        }
    }

    private void loadPage(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Node page = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ReservationController) {
                ((ReservationController) controller).setCurrentUser(currentUser.getId());
            } else if (controller instanceof PaymentController) {
                ((PaymentController) controller).setCurrentUser(currentUser.getId());
            } else if (controller instanceof ServiceRequestsController) {
                ((ServiceRequestsController) controller).setCurrentUser(currentUser.getId());
            } else if (controller instanceof ReportsController) {
                ((ReportsController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof BackupController) {
                ((BackupController) controller).setCurrentUser(currentUser.getId());
            } else if (controller instanceof UserManagementController) {
                ((UserManagementController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof RoomSearchController) {
                ((RoomSearchController) controller).setCurrentUser(currentUser);
            }

            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not load page: " + e.getMessage());
        }
    }

    private void showAccessDenied() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText(null);
        alert.setContentText("You do not have permission to access this module.");
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}