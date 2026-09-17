package com.gateway.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gateway.util.DBConnection;
import com.gateway.util.AuditLogger;

@WebServlet("/DeleteBorderServlet")
public class DeleteBorderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        String adminEmail = (String) session.getAttribute("userEmail");

        if (session == null || !role.equalsIgnoreCase("Admin")) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        String borderIdStr = request.getParameter("borderId");
        int borderId = 0;
        
        try {
            borderId = Integer.parseInt(borderIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("ViewBordersServlet?error=Invalid border ID.");
            return;
        }
        
        String redirectUrl = "ViewBordersServlet";

        try (Connection conn = DBConnection.getConnection()) {
            
            // To provide a meaningful log, we first fetch the location name
            String logInfo = fetchBorderInfo(conn, borderId); 
            
            String sql = "DELETE FROM borders WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, borderId);
            
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // AUDIT LOG: Border zone deleted
                AuditLogger.log(
                    adminEmail,
                    "LOGISTICS: Deleted border zone ID " + borderId + " (" + logInfo + ")"
                );
                redirectUrl += "?success=Border zone deleted successfully.";
            } else {
                redirectUrl += "?error=Failed to delete border zone. It may have already been removed.";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectUrl += "?error=Database Error: " + e.getMessage();
        }

        response.sendRedirect(redirectUrl);
    }
    
    // Helper method to retrieve info for the audit log
    private String fetchBorderInfo(Connection conn, int borderId) {
        String info = "Unknown Zone";
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT location_name, threat_level FROM borders WHERE id = ?");
            pstmt.setInt(1, borderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                info = rs.getString("location_name") + " (Threat: " + rs.getString("threat_level") + ")";
            }
        } catch (SQLException e) {
            // Log this exception internally but allow deletion to proceed
        }
        return info;
    }
}