package com.gateway.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditLogger {

    private static String getLatestHash(Connection conn) throws SQLException {
        String latestHash = "0";
        String query = "SELECT hash_current FROM audit_logs ORDER BY id DESC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                latestHash = rs.getString("hash_current");
            }
        }
        return latestHash;
    }

    public static void log(String userEmail, String action) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String hashPrevious = getLatestHash(conn);
            String logString = userEmail + ":" + action + ":" + System.currentTimeMillis();
            String hashInput = hashPrevious + logString;
            String hashCurrent = SHA256Util.generateHash(hashInput);

            String sql = "INSERT INTO audit_logs (user_email, action, hash_previous, hash_current) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userEmail);
                pstmt.setString(2, action);
                pstmt.setString(3, hashPrevious);
                pstmt.setString(4, hashCurrent);
                pstmt.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            AppLogger.logException("Failed to log audit event. Rolling back transaction.", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    AppLogger.logException("Failed to rollback audit transaction.", rollbackEx);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    AppLogger.logException("Failed to close audit connection.", closeEx);
                }
            }
        }
    }
}