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

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("user");

        if (username == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!newPassword.equals(confirmPassword)) {
            response.sendRedirect("change_password.jsp?error=Passwords+do+not+match");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT password FROM users WHERE username = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                if (!BCrypt.checkpw(currentPassword, storedHash)) {
                    response.sendRedirect("change_password.jsp?error=Incorrect+current+password");
                    return;
                }

              
                String newHashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                PreparedStatement update = con.prepareStatement("UPDATE users SET password = ? WHERE username = ?");
                update.setString(1, newHashed);
                update.setString(2, username);
                update.executeUpdate();

                AuditLogger.log(username, "Changed password");
                response.sendRedirect("dashboard.jsp?msg=Password+changed+successfully");
            } else {
                response.sendRedirect("login.jsp");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Database error: " + e.getMessage() + "</h3>");
        }
    }
}
