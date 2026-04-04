package service;

import db.DBConnection;
import model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {

    public boolean isRoomAvailable(int roomId, Date checkIn, Date checkOut) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                    "SELECT * FROM reservations WHERE room_id=? AND status IN ('Booked', 'Checked-in') " +
                            "AND ((check_in <= ? AND check_out > ?) OR (check_in < ? AND check_out >= ?))"
            );
            ps.setInt(1, roomId);
            ps.setDate(2, checkOut);
            ps.setDate(3, checkIn);
            ps.setDate(4, checkOut);
            ps.setDate(5, checkIn);

            rs = ps.executeQuery();
            return !rs.next();
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    public void addReservation(String guest, int room, Date in, Date out) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(
                    "INSERT INTO reservations (guest_name, room_id, check_in, check_out, status) VALUES (?, ?, ?, ?, 'Booked')"
            );
            ps.setString(1, guest);
            ps.setInt(2, room);
            ps.setDate(3, in);
            ps.setDate(4, out);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    public List<Reservation> getAllReservations() throws Exception {
        List<Reservation> list = new ArrayList<>();
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement("SELECT * FROM reservations ORDER BY id DESC");
            rs = ps.executeQuery();

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setGuestName(rs.getString("guest_name"));
                r.setRoomId(rs.getInt("room_id"));
                r.setCheckIn(rs.getDate("check_in"));
                r.setCheckOut(rs.getDate("check_out"));
                r.setStatus(rs.getString("status"));
                list.add(r);
            }
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
        return list;
    }

    public void deleteReservation(int id) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement("DELETE FROM reservations WHERE id=? AND status='Booked'");
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new Exception("Cannot delete reservation that is already checked-in");
            }
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    public void checkIn(int id) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(
                    "UPDATE reservations SET status='Checked-in' WHERE id=? AND status='Booked'"
            );
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new Exception("Reservation not found or already checked-in");
            }
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    public void checkOut(int id) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(
                    "UPDATE reservations SET status='Checked-out' WHERE id=? AND status='Checked-in'"
            );
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new Exception("Reservation not found or not checked-in");
            }
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
}