package com.gateway.util;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AuditLogger {
    public static void log(String username, String action) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO audit_log (username, action) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, action);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
