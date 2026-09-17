package com.gateway.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.gateway.util.DBConnection;
import com.gateway.util.AuditLogger;

@WebServlet("/DeleteWeaponServlet")
public class DeleteWeaponServlet extends HttpServlet {
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

        String weaponIdStr = request.getParameter("weaponId");
        int weaponId = 0;
        
        try {
            weaponId = Integer.parseInt(weaponIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("ViewWeaponsServlet?error=Invalid weapon ID.");
            return;
        }
        
        String redirectUrl = "ViewWeaponsServlet";

        try (Connection conn = DBConnection.getConnection()) {
            
            // To provide a meaningful log, we first fetch the name/serial number
            String logInfo = fetchWeaponInfo(conn, weaponId); 
            
            String sql = "DELETE FROM weapons WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, weaponId);
            
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // AUDIT LOG: Weapon deleted
                AuditLogger.log(
                    adminEmail,
                    "INVENTORY: Deleted asset ID " + weaponId + " (" + logInfo + ")"
                );
                redirectUrl += "?success=Asset deleted successfully.";
            } else {
                redirectUrl += "?error=Failed to delete asset. It may have already been removed.";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectUrl += "?error=Database Error: " + e.getMessage();
        }

        response.sendRedirect(redirectUrl);
    }
    
    // Helper method to retrieve info for the audit log
    private String fetchWeaponInfo(Connection conn, int weaponId) {
        String info = "Unknown Asset";
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT weapon_name, serial_number FROM weapons WHERE id = ?");
            pstmt.setInt(1, weaponId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                info = rs.getString("weapon_name") + " (SN: " + rs.getString("serial_number") + ")";
            }
        } catch (SQLException e) {
            // Log this exception internally but allow deletion to proceed
        }
        return info;
    }
}