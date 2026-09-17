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

@WebServlet("/UpdateBorderServlet")
public class UpdateBorderServlet extends HttpServlet {
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

        // Get parameters from the EditBorder.jsp form
        String idStr = request.getParameter("id");
        String locationName = request.getParameter("locationName"); // Used for logging
        String coordinates = request.getParameter("coordinates");
        String threatLevel = request.getParameter("threatLevel");

        int borderId = 0;
        try {
            borderId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("ViewBordersServlet?error=Invalid ID format.");
            return;
        }
        
        String redirectUrl = "ViewBordersServlet";

        try (Connection conn = DBConnection.getConnection()) {
            
            String sql = "UPDATE borders SET coordinates = ?, threat_level = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, coordinates);
            pstmt.setString(2, threatLevel);
            pstmt.setInt(3, borderId);
            
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // AUDIT LOG: Border updated
                AuditLogger.log(
                    adminEmail,
                    "LOGISTICS: Updated border zone " + locationName + " (Threat: " + threatLevel + ")"
                );
                redirectUrl += "?success=Border zone updated successfully!";
            } else {
                redirectUrl += "?error=Failed to update border zone. No changes made.";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectUrl += "?error=Database Error: " + e.getMessage();
        }

        response.sendRedirect(redirectUrl);
    }
}