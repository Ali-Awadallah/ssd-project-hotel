package security;

import db.DBConnection;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AuthService {

    public User login(String username, String password) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String trimmedUsername = username == null ? "" : username.trim();
            String trimmedPassword = password == null ? "" : password.trim();

            conn = DBConnection.connect();
            if (conn == null) {
                System.out.println("Database connection failed");
                return null;
            }

            ps = conn.prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND is_active = 1"
            );
            ps.setString(1, trimmedUsername);
            rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                int attempts = rs.getInt("failed_attempts");

                Timestamp lockedUntil = rs.getTimestamp("locked_until");
                if (lockedUntil != null && lockedUntil.after(new Timestamp(System.currentTimeMillis()))) {
                    System.out.println("Account locked until: " + lockedUntil);
                    return null;
                }

                if (attempts >= 5) {
                    lockAccount(trimmedUsername);
                    System.out.println("Account locked due to too many failed attempts");
                    return null;
                }

                String storedPassword = rs.getString("password");

                System.out.println("Username entered: " + trimmedUsername);
                System.out.println("Password entered: " + trimmedPassword);
                System.out.println("Stored hash: " + storedPassword);

                boolean passwordMatches = storedPassword != null && BCrypt.checkpw(trimmedPassword, storedPassword);
                System.out.println("BCrypt result: " + passwordMatches);

                if (passwordMatches) {
                    resetAttempts(trimmedUsername);

                    try {
                        log(userId, "Login success");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    User user = new User();
                    user.setId(userId);
                    user.setUsername(rs.getString("username"));
                    user.setRole(rs.getString("role"));
                    user.setPassword(storedPassword);

                    return user;
                } else {
                    increaseAttempts(trimmedUsername);

                    try {
                        logFailedLogin(trimmedUsername);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    logFailedLogin(username == null ? "" : username.trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void lockAccount(String username) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET locked_until = DATE_ADD(NOW(), INTERVAL 15 MINUTE) WHERE username=?"
        );
        ps.setString(1, username);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    private void increaseAttempts(String username) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE username=?"
        );
        ps.setString(1, username);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    private void resetAttempts(String username) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET failed_attempts = 0, locked_until = NULL, last_login = NOW() WHERE username=?"
        );
        ps.setString(1, username);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    private void log(int userId, String action) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO audit_logs(user_id, action) VALUES (?, ?)"
        );
        ps.setInt(1, userId);
        ps.setString(2, action);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    private void logFailedLogin(String username) throws Exception {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO audit_logs(action) VALUES (?)"
        );
        ps.setString(1, "Login failed for user: " + username);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }
}