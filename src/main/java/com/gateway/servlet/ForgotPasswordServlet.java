package com.gateway.servlet;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.gateway.util.DBConnection;
import com.gateway.util.MailSender;

@WebServlet("/ForgotPasswordServlet")
public class ForgotPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                response.sendRedirect("forgot.jsp?msg=Email not registered");
                return;
            }

            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO password_reset (email, token, expiry) VALUES (?, ?, ?)");
            insert.setString(1, email);
            insert.setString(2, token);
            insert.setTimestamp(3, Timestamp.valueOf(expiry));
            insert.executeUpdate();

            String resetLink = "http://localhost:8080/SecureGatewayAppCopy/reset.jsp?token=" + token;
            MailSender.send(email, "Password Reset Request", 
                "Click the link below to reset your password:\n" + resetLink);

            response.sendRedirect("forgot.jsp?msg=Reset link sent to your email.");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}
