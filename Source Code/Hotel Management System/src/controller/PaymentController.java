package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import service.PaymentService;
import security.AuditService;

public class PaymentController {

    @FXML private TextField reservationId;
    @FXML private TextField amount;

    private Integer currentUserId;

    public void setCurrentUser(Integer userId) {
        this.currentUserId = userId;
    }

    public void pay() {
        try {
            // Validate inputs
            if (reservationId.getText().trim().isEmpty()) {
                showAlert("Error", "Reservation ID is required!");
                return;
            }

            if (amount.getText().trim().isEmpty()) {
                showAlert("Error", "Amount is required!");
                return;
            }

            int resId = Integer.parseInt(reservationId.getText().trim());
            double amt = Double.parseDouble(amount.getText().trim());

            PaymentService service = new PaymentService();
            service.recordPayment(resId, amt);

            // Audit log
            if (currentUserId != null) {
                AuditService.log(currentUserId, "Recorded payment of $" + amt + " for reservation #" + resId);
            }

            showAlert("Success", "Payment of $" + amt + " recorded successfully!");

            // Clear form
            reservationId.clear();
            amount.clear();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for Reservation ID and Amount!");
        } catch (Exception e) {
            showAlert("Error", "Payment failed: " + e.getMessage());
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