package service;

import db.DBConnection;
import model.ServiceRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequestService {

    public void createServiceRequest(int reservationId, String requestType, String description) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(
                    "INSERT INTO service_requests (reservation_id, request_type, description, status) VALUES (?, ?, ?, 'Open')"
            );
            ps.setInt(1, reservationId);
            ps.setString(2, requestType);
            ps.setString(3, description);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    public List<ServiceRequest> getAllServiceRequests() throws Exception {
        List<ServiceRequest> list = new ArrayList<>();
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                    "SELECT * FROM service_requests ORDER BY created_at DESC"
            );
            rs = ps.executeQuery();

            while (rs.next()) {
                ServiceRequest sr = new ServiceRequest();
                sr.setId(rs.getInt("id"));
                sr.setReservationId(rs.getInt("reservation_id"));
                sr.setRequestType(rs.getString("request_type"));
                sr.setDescription(rs.getString("description"));
                sr.setStatus(rs.getString("status"));
                sr.setCreatedAt(rs.getTimestamp("created_at"));
                sr.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(sr);
            }
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
        return list;
    }

    public void updateServiceRequestStatus(int requestId, String status) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(
                    "UPDATE service_requests SET status = ? WHERE id = ?"
            );
            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
}