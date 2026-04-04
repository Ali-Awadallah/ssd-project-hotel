package security;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AuditService {

    public static void log(int userId, String action) {
        try {
            Connection conn = DBConnection.connect();

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO audit_logs(user_id, action) VALUES (?, ?)"
            );

            ps.setInt(1, userId);
            ps.setString(2, action);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}