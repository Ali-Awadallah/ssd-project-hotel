package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import service.ServiceRequestService;
import model.ServiceRequest;
import security.AuditService;
import java.sql.Timestamp;

public class ServiceRequestsController {

    @FXML private TextField reservationId;
    @FXML private ComboBox<String> requestType;
    @FXML private TextArea description;
    @FXML private TableView<ServiceRequest> table;
    @FXML private TableColumn<ServiceRequest, Integer> colId;
    @FXML private TableColumn<ServiceRequest, Integer> colReservationId;
    @FXML private TableColumn<ServiceRequest, String> colType;
    @FXML private TableColumn<ServiceRequest, String> colDescription;
    @FXML private TableColumn<ServiceRequest, String> colStatus;
    @FXML private TableColumn<ServiceRequest, Timestamp> colCreatedAt;

    private ServiceRequestService service;
    private ObservableList<ServiceRequest> requestList;
    private Integer currentUserId;

    public void setCurrentUser(Integer userId) {
        this.currentUserId = userId;
    }

    @FXML
    public void initialize() {
        service = new ServiceRequestService();
        requestList = FXCollections.observableArrayList();

        // Populate request type ComboBox
        requestType.getItems().addAll("Housekeeping", "Maintenance", "Room Service", "Laundry", "Other");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReservationId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colType.setCellValueFactory(new PropertyValueFactory<>("requestType"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        table.setItems(requestList);
        loadRequests();
    }

    public void createRequest() {
        try {
            if (reservationId.getText().trim().isEmpty()) {
                showAlert("Error", "Reservation ID is required!");
                return;
            }

            if (requestType.getValue() == null) {
                showAlert("Error", "Please select a request type!");
                return;
            }

            if (description.getText().trim().isEmpty()) {
                showAlert("Error", "Description is required!");
                return;
            }

            int resId = Integer.parseInt(reservationId.getText().trim());
            service.createServiceRequest(resId, requestType.getValue(), description.getText().trim());

            if (currentUserId != null) {
                AuditService.log(currentUserId, "Created service request for reservation #" + resId);
            }

            showAlert("Success", "Service request created successfully!");
            clearForm();
            loadRequests();

        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid reservation ID!");
        } catch (Exception e) {
            showAlert("Error", "Failed to create request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void markInProgress() {
        updateStatus("In Progress");
    }

    public void markCompleted() {
        updateStatus("Completed");
    }

    private void updateStatus(String status) {
        ServiceRequest selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a request!");
            return;
        }

        try {
            service.updateServiceRequestStatus(selected.getId(), status);
            if (currentUserId != null) {
                AuditService.log(currentUserId, "Updated service request #" + selected.getId() + " to " + status);
            }
            showAlert("Success", "Request marked as " + status);
            loadRequests();
        } catch (Exception e) {
            showAlert("Error", "Failed to update request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refresh() {
        loadRequests();
    }

    private void loadRequests() {
        try {
            requestList.clear();
            requestList.addAll(service.getAllServiceRequests());
        } catch (Exception e) {
            showAlert("Error", "Failed to load requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        reservationId.clear();
        requestType.setValue(null);
        description.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}