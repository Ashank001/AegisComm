package com.gateway.servlet;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.gateway.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/ResetPasswordServlet")
public class ResetPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!newPassword.equals(confirmPassword)) {
            response.sendRedirect("reset.jsp?token=" + token + "&msg=Passwords do not match");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT email, expiry FROM password_reset WHERE token = ?");
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String email = rs.getString("email");
                Timestamp expiry = rs.getTimestamp("expiry");

                if (expiry.toLocalDateTime().isBefore(LocalDateTime.now())) {
                    response.sendRedirect("reset.jsp?msg=Token expired");
                    return;
                }

                // Update password
                String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                PreparedStatement update = conn.prepareStatement(
                    "UPDATE users SET password = ? WHERE email = ?");
                update.setString(1, hashed);
                update.setString(2, email);
                update.executeUpdate();

                // Optional: delete token after use
                PreparedStatement delete = conn.prepareStatement("DELETE FROM password_reset WHERE token = ?");
                delete.setString(1, token);
                delete.executeUpdate();
                response.sendRedirect("login.jsp?success=Password reset successful");
            } else {
                response.sendRedirect("reset.jsp?msg=Invalid or expired token");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("DB Error: " + e.getMessage());
        }
    }
}
