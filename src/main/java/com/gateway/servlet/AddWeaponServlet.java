package com.gateway.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gateway.util.DBConnection;
import com.gateway.util.AuditLogger;

@WebServlet("/AddWeaponServlet")
public class AddWeaponServlet extends HttpServlet {
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

        String weaponName = request.getParameter("weaponName");
        String serialNumber = request.getParameter("serialNumber");
        String modelNumber = request.getParameter("modelNumber");
        String status = request.getParameter("status");
        String assignedToStr = request.getParameter("assignedTo");
        
        int assignedTo = Integer.parseInt(assignedToStr); // '0' for unassigned
        
        String redirectUrl = "AddWeapon.jsp";

        try (Connection conn = DBConnection.getConnection()) {
            
            String sql = "INSERT INTO weapons (serial_number, weapon_name, model_number, status, assigned_to) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, serialNumber);
            pstmt.setString(2, weaponName);
            pstmt.setString(3, modelNumber);
            pstmt.setString(4, status);
            
            // Set assigned_to to NULL if 0 (unassigned)
            if (assignedTo == 0) {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(5, assignedTo);
            }
            
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // AUDIT LOG: Weapon added
                AuditLogger.log(
                    adminEmail,
                    "INVENTORY: Added weapon " + weaponName + " (SN: " + serialNumber + ")"
                );
                redirectUrl += "?success=Weapon added successfully!";
            } else {
                redirectUrl += "?error=Failed to add weapon.";
            }
            
        } catch (SQLException e) {
            // Check for unique constraint violation (Duplicate Serial Number)
            if (e.getErrorCode() == 1062) { // MySQL error code for Duplicate entry
                 redirectUrl += "?error=Serial Number already exists. Cannot add duplicate weapon.";
            } else {
                 e.printStackTrace();
                 redirectUrl += "?error=Database Error: " + e.getMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectUrl += "?error=Server Error: " + e.getMessage();
        }

        response.sendRedirect(redirectUrl);
    }
}