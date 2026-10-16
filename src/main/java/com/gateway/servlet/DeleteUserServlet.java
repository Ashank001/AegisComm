package com.gateway.servlet;

import com.gateway.util.DBConnection;
import com.gateway.util.AuditLogger;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/DeleteUserServlet")
public class DeleteUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        // Security check: ensure only an admin is logged in
        if (session == null || !"Admin".equalsIgnoreCase((String) session.getAttribute("role"))) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        String adminEmail = (String) session.getAttribute("userEmail");
        String currentAdminId = (String) session.getAttribute("userId");
        String userIdStr = request.getParameter("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            response.sendRedirect("view_users.jsp?error=User ID not provided.");
            return;
        }

        if (userIdStr.equals(currentAdminId)) {
            response.sendRedirect("view_users.jsp?error=Cannot delete your own admin account.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(userIdStr));

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                AuditLogger.log(adminEmail, "DELETED USER: ID " + userIdStr);
                response.sendRedirect("view_users.jsp?success=User deleted successfully.");
            } else {
                response.sendRedirect("view_users.jsp?error=User not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("view_users.jsp?error=Server error deleting user.");
        }
    }
}