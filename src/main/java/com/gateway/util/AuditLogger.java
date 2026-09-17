package com.gateway.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditLogger {

    // Retrieves the current highest hash from the audit_logs table
    private static String getLatestHash(Connection conn) throws SQLException {
        String latestHash = "0"; // Default hash for the very first log entry
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
            conn.setAutoCommit(false); // Start transaction for atomic operation

            // 1. Get the hash of the most recent log entry
            String hashPrevious = getLatestHash(conn);
            
            // 2. Build the string to be hashed for the new log entry
            String logString = userEmail + ":" + action + ":" + System.currentTimeMillis();
            
            // 3. Calculate the new hash by chaining it to the previous hash
            String hashInput = hashPrevious + logString;
            String hashCurrent = SHA256Util.generateHash(hashInput);

            // 4. Insert the new log entry into the database
            String sql = "INSERT INTO audit_logs (user_email, action, hash_previous, hash_current) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userEmail);
                pstmt.setString(2, action);
                pstmt.setString(3, hashPrevious);
                pstmt.setString(4, hashCurrent);
                pstmt.executeUpdate();
            }
            
            conn.commit(); // Commit the transaction
            
        } catch (Exception e) {
            System.err.println("Failed to log audit event. Rolling back transaction: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }
}