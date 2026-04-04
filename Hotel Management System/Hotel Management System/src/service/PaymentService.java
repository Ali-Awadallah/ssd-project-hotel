package service;

import db.DBConnection;
import java.sql.*;

public class PaymentService {

    public void recordPayment(int reservationId, double amount) throws Exception {
        if (amount <= 0) {
            throw new Exception("Invalid amount. Amount must be greater than 0.");
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false);

            // Check if reservation exists
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT id FROM reservations WHERE id=?"
            );
            checkStmt.setInt(1, reservationId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                throw new Exception("Reservation not found!");
            }

            // Record payment
            ps = conn.prepareStatement(
                    "INSERT INTO payments(reservation_id, amount, date) VALUES (?, ?, NOW())"
            );
            ps.setInt(1, reservationId);
            ps.setDouble(2, amount);
            ps.executeUpdate();

            conn.commit();
            System.out.println("Payment recorded successfully for reservation #" + reservationId);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    public double getTotalPayments(int reservationId) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                    "SELECT SUM(amount) as total FROM payments WHERE reservation_id=?"
            );
            ps.setInt(1, reservationId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
}