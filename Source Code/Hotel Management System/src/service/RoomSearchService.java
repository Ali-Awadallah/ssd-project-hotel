package service;

import db.DBConnection;
import model.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomSearchService {

    public List<Room> searchAvailableRooms(Date checkIn, Date checkOut, String roomType) throws Exception {
        List<Room> availableRooms = new ArrayList<>();
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM rooms WHERE status = 'Available'";

            if (roomType != null && !roomType.isEmpty()) {
                sql += " AND type = ?";
            }

            sql += " AND id NOT IN (SELECT room_id FROM reservations WHERE status IN ('Booked', 'Checked-in') " +
                    "AND ((check_in <= ? AND check_out > ?) OR (check_in < ? AND check_out >= ?)))";

            ps = conn.prepareStatement(sql);
            int paramIndex = 1;

            if (roomType != null && !roomType.isEmpty()) {
                ps.setString(paramIndex++, roomType);
            }

            ps.setDate(paramIndex++, checkOut);
            ps.setDate(paramIndex++, checkIn);
            ps.setDate(paramIndex++, checkOut);
            ps.setDate(paramIndex++, checkIn);

            rs = ps.executeQuery();

            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("id"));
                room.setRoomNumber(rs.getInt("room_number"));
                room.setType(rs.getString("type"));
                room.setStatus(rs.getString("status"));
                room.setPricePerNight(rs.getDouble("price_per_night"));
                availableRooms.add(room);
            }
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
        return availableRooms;
    }

    public List<String> getRoomTypes() throws Exception {
        List<String> types = new ArrayList<>();
        Connection conn = DBConnection.connect();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT DISTINCT type FROM rooms ORDER BY type");

            while (rs.next()) {
                types.add(rs.getString("type"));
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        return types;
    }
}