package com.gateway.servlet;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;
import com.gateway.util.DBConnection;

@WebServlet("/SignupServlet")
public class SignupServlet extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        try (Connection conn = DBConnection.getConnection()) {

            // Check for duplicate email
            PreparedStatement check = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            check.setString(1, email);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                response.sendRedirect("Signup.jsp?error=Email already registered");
                return;
            }

            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hashed);
            ps.setString(4, role);

            int i = ps.executeUpdate();
            if (i > 0) {
                response.sendRedirect("login.jsp?success=Account created successfully");
            } else {
                response.sendRedirect("Signup.jsp?error=Registration failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("DB Error: " + e.getMessage());
        }
    }
}
