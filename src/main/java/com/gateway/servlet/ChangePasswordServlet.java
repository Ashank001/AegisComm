package com.gateway.servlet;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.gateway.util.DBConnection;
import com.gateway.util.AuditLogger;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/ChangePasswordServlet")
public class ChangePasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String userEmail = (session != null) ? (String) session.getAttribute("userEmail") : null;
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        if (userEmail == null) {
            response.sendRedirect("login.jsp?error=Session expired. Please log in again.");
            return;
        }

        // Determine the correct dashboard for redirects
        String dashboardPath = getDashboardPath(role);

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!newPassword.equals(confirmPassword)) {
            response.sendRedirect("change_password.jsp?error=Passwords+do+not+match");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT password FROM users WHERE email = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, userEmail);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                if (!BCrypt.checkpw(currentPassword, storedHash)) {
                    response.sendRedirect("change_password.jsp?error=Incorrect+current+password");
                    return;
                }

                String newHashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                PreparedStatement update = conn.prepareStatement("UPDATE users SET password = ? WHERE email = ?");
                update.setString(1, newHashed);
                update.setString(2, userEmail);
                update.executeUpdate();

                AuditLogger.log(userEmail, "Changed password");
                response.sendRedirect(dashboardPath + "?msg=Password+changed+successfully");
            } else {
                response.sendRedirect("login.jsp?error=Account not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("change_password.jsp?error=Server+error.+Please+try+again.");
        }
    }

    private String getDashboardPath(String role) {
        if (role == null) return "login.jsp";
        if (role.equalsIgnoreCase("Admin")) return "dashboard.jsp";
        if (role.equalsIgnoreCase("Intelligence")) return "dashboard_intel.jsp";
        if (role.equalsIgnoreCase("TopOrder")) return "dashboard_top.jsp";
        if (role.equalsIgnoreCase("Secondary")) return "dashboard_second.jsp";
        return "dashboard_soldier.jsp";
    }
}
