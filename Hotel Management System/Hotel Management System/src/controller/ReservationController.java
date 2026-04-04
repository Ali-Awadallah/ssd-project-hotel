package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import service.ReservationService;
import model.Reservation;
import model.Room;
import security.AuditService;
import db.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ReservationController {

    @FXML private TextField guestName;
    @FXML private ComboBox<Room> roomCombo;
    @FXML private TextField checkIn;
    @FXML private TextField checkOut;
    @FXML private TableView<Reservation> table;
    @FXML private TableColumn<Reservation, Integer> colId;
    @FXML private TableColumn<Reservation, String> colGuest;
    @FXML private TableColumn<Reservation, Integer> colRoom;
    @FXML private TableColumn<Reservation, Date> colCheckIn;
    @FXML private TableColumn<Reservation, Date> colCheckOut;
    @FXML private TableColumn<Reservation, String> colStatus;

    private ReservationService reservationService;
    private ObservableList<Reservation> reservationList;
    private ObservableList<Room> roomList;
    private Integer currentUserId;

    public void setCurrentUser(Integer userId) {
        this.currentUserId = userId;
    }

    @FXML
    public void initialize() {
        reservationService = new ReservationService();
        reservationList = FXCollections.observableArrayList();
        roomList = FXCollections.observableArrayList();

        // Set up table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colGuest.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.setItems(reservationList);

        // Set up room ComboBox
        roomCombo.setCellFactory(lv -> new ListCell<Room>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null) {
                    setText(null);
                } else {
                    setText("Room " + room.getRoomNumber() + " - " + room.getType() + " ($" + room.getPricePerNight() + "/night)");
                }
            }
        });

        roomCombo.setButtonCell(new ListCell<Room>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null) {
                    setText("Select a room");
                } else {
                    setText("Room " + room.getRoomNumber() + " - " + room.getType());
                }
            }
        });

        // Load rooms and reservations
        loadRooms();
        loadReservations();
    }

    private void loadRooms() {
        try {
            roomList.clear();
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM rooms WHERE status = 'Available' ORDER BY room_number");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("id"));
                room.setRoomNumber(rs.getInt("room_number"));
                room.setType(rs.getString("type"));
                room.setStatus(rs.getString("status"));
                room.setPricePerNight(rs.getDouble("price_per_night"));
                roomList.add(room);
            }

            roomCombo.setItems(roomList);
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load rooms: " + e.getMessage());
        }
    }

    public void createReservation() {
        try {
            // Validate inputs
            if (guestName.getText().trim().isEmpty()) {
                showAlert("Error", "Guest name is required!");
                return;
            }

            if (roomCombo.getValue() == null) {
                showAlert("Error", "Please select a room!");
                return;
            }

            // Parse dates
            Date in, out;
            try {
                in = Date.valueOf(checkIn.getText());
                out = Date.valueOf(checkOut.getText());
            } catch (DateTimeParseException e) {
                showAlert("Error", "Invalid date format! Use YYYY-MM-DD");
                return;
            }

            // Validate dates
            if (out.before(in) || out.equals(in)) {
                showAlert("Error", "Check-out date must be after check-in date!");
                return;
            }

            LocalDate today = LocalDate.now();
            if (in.toLocalDate().isBefore(today)) {
                showAlert("Error", "Check-in date cannot be in the past!");
                return;
            }

            int roomId = roomCombo.getValue().getId();

            // Check availability
            boolean available = reservationService.isRoomAvailable(roomId, in, out);

            if (!available) {
                showAlert("Error", "Room is not available for the selected dates!");
                return;
            }

            // Create reservation
            reservationService.addReservation(guestName.getText().trim(), roomId, in, out);

            // Audit log
            if (currentUserId != null) {
                AuditService.log(currentUserId, "Created reservation for guest: " + guestName.getText().trim() + ", Room: " + roomCombo.getValue().getRoomNumber());
            }

            showAlert("Success", "Reservation created successfully!");

            // Clear form
            clearForm();

            // Refresh table and rooms
            loadRooms();
            loadReservations();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "System error: " + e.getMessage());
        }
    }

    public void deleteReservation() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a reservation to delete!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Are you sure you want to delete reservation #" + selected.getId() + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                reservationService.deleteReservation(selected.getId());

                if (currentUserId != null) {
                    AuditService.log(currentUserId, "Deleted reservation #" + selected.getId());
                }

                showAlert("Success", "Reservation deleted successfully!");
                loadReservations();
                loadRooms(); // Refresh available rooms
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to delete reservation!");
            }
        }
    }

    public void checkIn() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a reservation for check-in!");
            return;
        }

        if (!"Booked".equals(selected.getStatus())) {
            showAlert("Error", "Reservation is not in 'Booked' status!");
            return;
        }

        try {
            reservationService.checkIn(selected.getId());

            if (currentUserId != null) {
                AuditService.log(currentUserId, "Checked-in reservation #" + selected.getId());
            }

            showAlert("Success", "Check-in completed successfully!");
            loadReservations();
            loadRooms(); // Refresh available rooms
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to check-in!");
        }
    }

    public void checkOut() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a reservation for check-out!");
            return;
        }

        if (!"Checked-in".equals(selected.getStatus())) {
            showAlert("Error", "Reservation is not in 'Checked-in' status!");
            return;
        }

        try {
            reservationService.checkOut(selected.getId());

            if (currentUserId != null) {
                AuditService.log(currentUserId, "Checked-out reservation #" + selected.getId());
            }

            showAlert("Success", "Check-out completed successfully!");
            loadReservations();
            loadRooms(); // Refresh available rooms
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to check-out!");
        }
    }

    public void clearForm() {
        guestName.clear();
        roomCombo.setValue(null);
        checkIn.clear();
        checkOut.clear();
    }

    private void loadReservations() {
        try {
            reservationList.clear();
            reservationList.addAll(reservationService.getAllReservations());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load reservations!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}