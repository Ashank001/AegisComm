package com.gateway.servlet;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.net.URLEncoder;

import com.gateway.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import com.gateway.util.AuditLogger; // <-- Ensure this import is present

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            response.sendRedirect("login.jsp?error=Please enter both email and password");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("PASSWORD");

                if (BCrypt.checkpw(password, storedHash)) {
                    // Set session
                    HttpSession session = request.getSession();
                    session.setAttribute("userEmail", email);
                    session.setAttribute("userId", String.valueOf(rs.getInt("id")));
                    session.setAttribute("role", rs.getString("ROLE"));
                    session.setAttribute("userName", rs.getString("NAME"));
                    session.setAttribute("profileImg", rs.getString("profileImg"));
                    
                    // --- AUDIT LOG INTEGRATION (CRITICAL LINE) ---
                    AuditLogger.log(email, "User logged in successfully.");

                    String role = rs.getString("ROLE");
                    if (role.equalsIgnoreCase("admin")) {
                        response.sendRedirect("dashboard.jsp");
                    } else if (role.equalsIgnoreCase("Intelligence")) {
                        response.sendRedirect("dashboard_intel.jsp");
                    } else if (role.equalsIgnoreCase("TopOrder")) {
                        response.sendRedirect("dashboard_top.jsp");
                    } else if (role.equalsIgnoreCase("Secondary")) {
                        response.sendRedirect("dashboard_second.jsp");
                    } else {
                        response.sendRedirect("dashboard_soldier.jsp");
                    }
                } else {
                    // Log failed attempt (optional, but good security practice)
                    AuditLogger.log(email, "Login failed: Invalid password.");
                    response.sendRedirect("login.jsp?error=Invalid Credentials");
                }
            } else {
                String msg = URLEncoder.encode("Email not registered or not authorized.", "UTF-8");
                // Log failed attempt
                AuditLogger.log(email, "Login failed: Email not found in database.");
                response.sendRedirect("login.jsp?error=" + msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Database Error: " + e.getMessage() + "</h3>");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Unexpected Error: " + e.getMessage() + "</h3>");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET method not supported.");
    }
}