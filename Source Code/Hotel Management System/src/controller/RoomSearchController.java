package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import service.RoomSearchService;
import model.Room;
import model.User;
import java.sql.Date;
import java.time.LocalDate;

public class RoomSearchController {

    @FXML private TextField checkIn;
    @FXML private TextField checkOut;
    @FXML private ComboBox<String> roomType;
    @FXML private TableView<Room> table;
    @FXML private TableColumn<Room, Integer> colRoomNumber;
    @FXML private TableColumn<Room, String> colType;
    @FXML private TableColumn<Room, Double> colPrice;
    @FXML private TableColumn<Room, String> colStatus;

    private RoomSearchService service;
    private ObservableList<Room> roomList;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        service = new RoomSearchService();
        roomList = FXCollections.observableArrayList();

        colRoomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.setItems(roomList);

        // Load room types - add "All Types" first, then add actual types
        roomType.getItems().clear();
        roomType.getItems().add("All Types");
        try {
            roomType.getItems().addAll(service.getRoomTypes());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load room types: " + e.getMessage());
        }
        roomType.setValue("All Types");
    }

    public void search() {
        try {
            if (checkIn.getText().trim().isEmpty() || checkOut.getText().trim().isEmpty()) {
                showAlert("Error", "Please enter both check-in and check-out dates!");
                return;
            }

            Date in = Date.valueOf(checkIn.getText());
            Date out = Date.valueOf(checkOut.getText());

            if (out.before(in) || out.equals(in)) {
                showAlert("Error", "Check-out must be after check-in!");
                return;
            }

            String type = roomType.getValue();
            if ("All Types".equals(type)) {
                type = null;
            }

            roomList.clear();
            roomList.addAll(service.searchAvailableRooms(in, out, type));

            if (roomList.isEmpty()) {
                showAlert("Info", "No rooms available for the selected dates.");
            }

        } catch (IllegalArgumentException e) {
            showAlert("Error", "Invalid date format! Use YYYY-MM-DD (e.g., 2026-04-01)");
        } catch (Exception e) {
            showAlert("Error", "Search failed: " + e.getMessage());
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