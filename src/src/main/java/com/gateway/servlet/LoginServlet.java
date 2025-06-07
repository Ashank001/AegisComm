package com.gateway.servlet;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.gateway.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import com.gateway.util.AuditLogger;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Basic validation
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            response.sendRedirect("login.jsp?error=Please enter both username and password");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

          
                System.out.println("Username: " + username);
                System.out.println("Password entered: " + password);
                System.out.println("Stored hash: " + storedHash);
                System.out.println("BCrypt match result: " + BCrypt.checkpw(password, storedHash));

                if (BCrypt.checkpw(password, storedHash)) {
                    // Set session
                    HttpSession session = request.getSession();
                    session.setAttribute("user", username);
                    session.setAttribute("role", rs.getString("role"));

                
                    AuditLogger.log(username, "Logged in");

                   
                    response.sendRedirect("dashboard.jsp");
                } else {
                    response.sendRedirect("login.jsp?error=Invalid Credentials");
                }
            } else {
                response.sendRedirect("login.jsp?error=Invalid Credentials");
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
