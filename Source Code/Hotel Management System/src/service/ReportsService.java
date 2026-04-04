package service;

import db.DBConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ReportsService {

    public Map<String, Object> getDailyReport(Date date) throws Exception {
        Map<String, Object> report = new HashMap<>();
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Arrivals (check-in today)
            ps = conn.prepareStatement(
                    "SELECT COUNT(*) as arrivals FROM reservations WHERE check_in = ? AND status = 'Booked'"
            );
            ps.setDate(1, date);
            rs = ps.executeQuery();
            if (rs.next()) report.put("arrivals", rs.getInt("arrivals"));

            // Departures (check-out today)
            ps = conn.prepareStatement(
                    "SELECT COUNT(*) as departures FROM reservations WHERE check_out = ? AND status = 'Checked-in'"
            );
            ps.setDate(1, date);
            rs = ps.executeQuery();
            if (rs.next()) report.put("departures", rs.getInt("departures"));

            // In-house guests
            ps = conn.prepareStatement(
                    "SELECT COUNT(*) as in_house FROM reservations WHERE check_in <= ? AND check_out > ? AND status = 'Checked-in'"
            );
            ps.setDate(1, date);
            ps.setDate(2, date);
            rs = ps.executeQuery();
            if (rs.next()) report.put("in_house", rs.getInt("in_house"));

            // Occupancy rate
            ps = conn.prepareStatement(
                    "SELECT COUNT(*) as occupied FROM rooms WHERE status = 'Occupied'"
            );
            rs = ps.executeQuery();
            if (rs.next()) {
                int occupied = rs.getInt("occupied");
                int totalRooms = 0;
                Statement stmt = conn.createStatement();
                ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) as total FROM rooms");
                if (rs2.next()) totalRooms = rs2.getInt("total");
                report.put("occupancy_rate", totalRooms > 0 ? (double) occupied / totalRooms * 100 : 0);
            }

            // Revenue for today
            ps = conn.prepareStatement(
                    "SELECT SUM(amount) as revenue FROM payments WHERE DATE(date) = ?"
            );
            ps.setDate(1, date);
            rs = ps.executeQuery();
            if (rs.next()) report.put("revenue", rs.getDouble("revenue"));

        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
        return report;
    }

    public Map<String, Object> getRevenueReport(Date startDate, Date endDate) throws Exception {
        Map<String, Object> report = new HashMap<>();
        Connection conn = DBConnection.connect();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                    "SELECT DATE(date) as payment_date, SUM(amount) as daily_revenue " +
                            "FROM payments WHERE DATE(date) BETWEEN ? AND ? " +
                            "GROUP BY DATE(date) ORDER BY payment_date"
            );
            ps.setDate(1, startDate);
            ps.setDate(2, endDate);
            rs = ps.executeQuery();

            Map<Date, Double> dailyRevenue = new HashMap<>();
            double totalRevenue = 0;

            while (rs.next()) {
                Date paymentDate = rs.getDate("payment_date");
                double revenue = rs.getDouble("daily_revenue");
                dailyRevenue.put(paymentDate, revenue);
                totalRevenue += revenue;
            }

            report.put("daily_revenue", dailyRevenue);
            report.put("total_revenue", totalRevenue);
            report.put("start_date", startDate);
            report.put("end_date", endDate);

        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
        return report;
    }
}