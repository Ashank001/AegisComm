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

import com.gateway.util.DBConnection;
import com.gateway.util.AuditLogger;

@WebServlet("/UpdateWeaponServlet")
public class UpdateWeaponServlet extends HttpServlet {
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

        // Get parameters from the EditWeapon.jsp form
        String idStr = request.getParameter("id");
        String weaponName = request.getParameter("weaponName");
        String modelNumber = request.getParameter("modelNumber");
        String status = request.getParameter("status");
        String assignedToStr = request.getParameter("assignedTo");
        String serialNumber = request.getParameter("serialNumber"); // Used for logging

        int weaponId = 0;
        int assignedTo = 0;
        
        try {
            weaponId = Integer.parseInt(idStr);
            assignedTo = Integer.parseInt(assignedToStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("ViewWeaponsServlet?error=Invalid ID or Assignment format.");
            return;
        }
        
        String redirectUrl = "ViewWeaponsServlet";

        try (Connection conn = DBConnection.getConnection()) {
            
            // SQL: Update only name, model, status, and assignment. Serial Number is static.
            String sql = "UPDATE weapons SET weapon_name = ?, model_number = ?, status = ?, assigned_to = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, weaponName);
            pstmt.setString(2, modelNumber);
            pstmt.setString(3, status);
            
            // Set assigned_to to NULL if 0 (unassigned)
            if (assignedTo == 0) {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(4, assignedTo);
            }
            
            pstmt.setInt(5, weaponId);
            
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // AUDIT LOG: Weapon updated
                AuditLogger.log(
                    adminEmail,
                    "INVENTORY: Updated asset " + weaponName + " (SN: " + serialNumber + ")"
                );
                redirectUrl += "?success=Asset updated successfully!";
            } else {
                redirectUrl += "?error=Failed to update asset. No changes made.";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectUrl += "?error=Database Error: " + e.getMessage();
        }

        response.sendRedirect(redirectUrl);
    }
}
